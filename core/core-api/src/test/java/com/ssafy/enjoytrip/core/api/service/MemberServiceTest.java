package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.EMAIL_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_CREDENTIALS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.AuthLogRecord;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AuthLogMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class MemberServiceTest {
    private MemberMapper memberMapper;
    private AuthLogMapper authLogMapper;
    private PasswordEncoder passwordEncoder;
    private MemberService service;

    @BeforeEach
    void setUp() {
        memberMapper = mock(MemberMapper.class);
        authLogMapper = mock(AuthLogMapper.class);
        passwordEncoder = new BCryptPasswordEncoder();
        service = new MemberService(passwordEncoder, memberMapper, authLogMapper);
    }

    @DisplayName("회원가입은 BCrypt 비밀번호를 db-core MemberRecord로 저장한다")
    @Test
    void signupStoresBcryptPassword() {
        when(memberMapper.existsByUserId("ssafy")).thenReturn(0);
        when(memberMapper.existsByEmail("ssafy@example.com")).thenReturn(0);

        service.signup(new Member("ssafy", "SSAFY", "ssafy@example.com", "secret", ""));

        ArgumentCaptor<MemberRecord> memberCaptor = ArgumentCaptor.forClass(MemberRecord.class);
        verify(memberMapper).insert(memberCaptor.capture());
        MemberRecord saved = memberCaptor.getValue();
        assertThat(saved.getPassword()).isNotEqualTo("secret");
        assertThat(passwordEncoder.matches("secret", saved.getPassword())).isTrue();
    }

    @DisplayName("회원가입은 중복 이메일을 거부한다")
    @Test
    void signupRejectsDuplicateEmail() {
        when(memberMapper.existsByUserId("ssafy")).thenReturn(0);
        when(memberMapper.existsByEmail("ssafy@example.com")).thenReturn(1);

        assertThatThrownBy(() -> service.signup(new Member("ssafy", "SSAFY", "ssafy@example.com", "secret", "")))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(EMAIL_ALREADY_EXISTS));

        verify(memberMapper, never()).insert(any());
    }

    @DisplayName("로그인은 비밀번호가 일치하면 회원을 반환하고 로그인 로그를 저장한다")
    @Test
    void loginReturnsMemberWhenPasswordMatches() {
        String encodedPassword = passwordEncoder.encode("secret");
        MemberRecord record = new MemberRecord("ssafy", "SSAFY", null, "ssafy@example.com", encodedPassword,
                "", null, null, null);
        when(memberMapper.findByUserId("ssafy")).thenReturn(record);

        Member loggedIn = service.login("ssafy", "secret");

        assertThat(loggedIn.userId()).isEqualTo("ssafy");
        verify(authLogMapper).insert(any(AuthLogRecord.class));
        verify(memberMapper, never()).update(any());
    }

    @DisplayName("로그인은 비밀번호가 일치하지 않으면 실패한다")
    @Test
    void loginFailsWhenPasswordDoesNotMatch() {
        MemberRecord record = new MemberRecord("ssafy", "SSAFY", null, "ssafy@example.com",
                passwordEncoder.encode("secret"), "", null, null, null);
        when(memberMapper.findByUserId("ssafy")).thenReturn(record);

        assertThatThrownBy(() -> service.login("ssafy", "wrong"))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(INVALID_CREDENTIALS));

        verify(authLogMapper, never()).insert(any());
    }

    @DisplayName("OAuth 회원가입은 이름과 닉네임을 분리해 저장한다")
    @Test
    void oauthSignupStoresNicknameSeparatelyFromName() {
        when(memberMapper.findByEmail("google@example.com")).thenReturn(null);
        when(memberMapper.existsByUserId("google_123")).thenReturn(0);

        Member member = service.signupWithOAuth("google", "123", "google@example.com", "김구글", "트래블러");

        ArgumentCaptor<MemberRecord> memberCaptor = ArgumentCaptor.forClass(MemberRecord.class);
        verify(memberMapper).insert(memberCaptor.capture());
        MemberRecord saved = memberCaptor.getValue();
        assertThat(member.name()).isEqualTo("김구글");
        assertThat(member.nickname()).isEqualTo("트래블러");
        assertThat(saved.getName()).isEqualTo("김구글");
        assertThat(saved.getNickname()).isEqualTo("트래블러");
    }
}

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
import com.ssafy.enjoytrip.core.domain.auth.PasswordCodec;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.entity.AuthLogEntity;
import com.ssafy.enjoytrip.storage.db.core.entity.MemberEntity;
import com.ssafy.enjoytrip.storage.db.core.jpa.AuthLogJpaRepository;
import com.ssafy.enjoytrip.storage.db.core.jpa.MemberJpaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class MemberServiceTest {
    private MemberJpaRepository memberRepository;
    private AuthLogJpaRepository authLogRepository;
    private PasswordEncoder passwordEncoder;
    private PasswordCodec passwordCodec;
    private MemberService service;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberJpaRepository.class);
        authLogRepository = mock(AuthLogJpaRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        passwordCodec = passwordCodec(passwordEncoder);
        service = new MemberService(passwordCodec, memberRepository, authLogRepository);
    }

    @DisplayName("회원가입은 BCrypt 비밀번호를 db-core MemberEntity로 저장한다")
    @Test
    void signupStoresBcryptPassword() {
        when(memberRepository.existsByUserId("ssafy")).thenReturn(false);
        when(memberRepository.existsByEmail("ssafy@example.com")).thenReturn(false);

        service.signup(new Member("ssafy", "SSAFY", "ssafy@example.com", "secret", ""));

        ArgumentCaptor<MemberEntity> memberCaptor = ArgumentCaptor.forClass(MemberEntity.class);
        verify(memberRepository).save(memberCaptor.capture());
        MemberEntity saved = memberCaptor.getValue();
        assertThat(saved.getPassword()).isNotEqualTo("secret");
        assertThat(passwordEncoder.matches("secret", saved.getPassword())).isTrue();
    }

    @DisplayName("회원가입은 중복 이메일을 거부한다")
    @Test
    void signupRejectsDuplicateEmail() {
        when(memberRepository.existsByUserId("ssafy")).thenReturn(false);
        when(memberRepository.existsByEmail("ssafy@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.signup(new Member("ssafy", "SSAFY", "ssafy@example.com", "secret", "")))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(EMAIL_ALREADY_EXISTS));

        verify(memberRepository, never()).save(any());
    }

    @DisplayName("로그인은 비밀번호가 일치하면 회원을 반환하고 로그인 로그를 저장한다")
    @Test
    void loginReturnsMemberWhenPasswordMatches() {
        String encodedPassword = passwordEncoder.encode("secret");
        MemberEntity entity = new MemberEntity("ssafy", "SSAFY", null, "ssafy@example.com", encodedPassword,
                "", null, null, null);
        when(memberRepository.findByUserId("ssafy")).thenReturn(Optional.of(entity));

        Member loggedIn = service.login("ssafy", "secret");

        assertThat(loggedIn.userId()).isEqualTo("ssafy");
        verify(authLogRepository).save(any(AuthLogEntity.class));
        verify(memberRepository, never()).save(any());
    }

    @DisplayName("로그인은 비밀번호가 일치하지 않으면 실패한다")
    @Test
    void loginFailsWhenPasswordDoesNotMatch() {
        MemberEntity entity = new MemberEntity("ssafy", "SSAFY", null, "ssafy@example.com",
                passwordEncoder.encode("secret"), "", null, null, null);
        when(memberRepository.findByUserId("ssafy")).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.login("ssafy", "wrong"))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(INVALID_CREDENTIALS));

        verify(authLogRepository, never()).save(any());
    }

    private static PasswordCodec passwordCodec(PasswordEncoder passwordEncoder) {
        return new PasswordCodec() {
            @Override
            public String encode(String rawPassword) {
                return passwordEncoder.encode(rawPassword);
            }

            @Override
            public boolean matches(String rawPassword, String encodedPassword) {
                return passwordEncoder.matches(rawPassword, encodedPassword);
            }

            @Override
            public boolean isEncoded(String password) {
                if (password == null) {
                    return false;
                }
                return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
            }
        };
    }
}

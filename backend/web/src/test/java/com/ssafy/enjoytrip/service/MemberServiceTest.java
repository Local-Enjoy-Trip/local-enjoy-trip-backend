package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.support.error.ErrorType.EMAIL_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_CREDENTIALS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.repository.MemberRepository;
import com.ssafy.enjoytrip.security.PasswordCodec;
import com.ssafy.enjoytrip.support.error.CoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class MemberServiceTest {
    private MemberRepository repository;
    private PasswordEncoder passwordEncoder;
    private PasswordCodec passwordCodec;
    private MemberService service;

    @BeforeEach
    void setUp() {
        repository = mock(MemberRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        passwordCodec = passwordCodec(passwordEncoder);
        service = new MemberService(repository, passwordCodec);
    }

    @DisplayName("회원가입은 BCrypt 비밀번호를 저장한다")
    @Test
    void signupStoresBcryptPassword() {
        when(repository.existsByUserId("ssafy")).thenReturn(false);
        when(repository.existsByEmail("ssafy@example.com")).thenReturn(false);

        service.signup(new Member("ssafy", "SSAFY", "ssafy@example.com", "secret", ""));

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(repository).insert(memberCaptor.capture());
        Member saved = memberCaptor.getValue();
        assertThat(saved.password()).isNotEqualTo("secret");
        assertThat(passwordEncoder.matches("secret", saved.password())).isTrue();
    }

    @DisplayName("회원가입은 중복 이메일을 거부한다")
    @Test
    void signupRejectsDuplicateEmail() {
        when(repository.existsByUserId("ssafy")).thenReturn(false);
        when(repository.existsByEmail("ssafy@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.signup(new Member("ssafy", "SSAFY", "ssafy@example.com", "secret", "")))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(EMAIL_ALREADY_EXISTS));

        verify(repository, never()).insert(any());
    }

    @DisplayName("로그인은 비밀번호가 일치하면 회원을 반환한다")
    @Test
    void loginReturnsMemberWhenPasswordMatches() {
        String encodedPassword = passwordEncoder.encode("secret");
        Member member = new Member("ssafy", "SSAFY", "ssafy@example.com", encodedPassword, "");
        when(repository.findByUserId("ssafy")).thenReturn(member);

        Member loggedIn = service.login("ssafy", "secret");

        assertThat(loggedIn).isEqualTo(member);
        verify(repository).insertAuthLog("ssafy", "LOGIN");
        verify(repository, never()).update(any());
    }

    @DisplayName("로그인은 비밀번호가 일치하지 않으면 실패한다")
    @Test
    void loginFailsWhenPasswordDoesNotMatch() {
        Member member = new Member("ssafy", "SSAFY", "ssafy@example.com", passwordEncoder.encode("secret"), "");
        when(repository.findByUserId("ssafy")).thenReturn(member);

        assertThatThrownBy(() -> service.login("ssafy", "wrong"))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(INVALID_CREDENTIALS));

        verify(repository, never()).insertAuthLog(any(), any());
    }

    @DisplayName("로그인은 평문 비밀번호를 BCrypt로 마이그레이션한다")
    @Test
    void loginMigratesPlainTextPasswordToBcrypt() {
        Member member = new Member("ssafy", "SSAFY", "ssafy@example.com", "secret", "");
        when(repository.findByUserId("ssafy")).thenReturn(member);

        Member loggedIn = service.login("ssafy", "secret");

        assertThat(loggedIn).isEqualTo(member);
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(repository).update(memberCaptor.capture());
        assertThat(passwordEncoder.matches("secret", memberCaptor.getValue().password())).isTrue();
        verify(repository).insertAuthLog("ssafy", "LOGIN");
    }

    @DisplayName("OAuth 로그인은 기존 이메일 회원을 재사용한다")
    @Test
    void oauthLoginReusesExistingEmail() {
        Member member = new Member("ssafy", "SSAFY", "ssafy@example.com", passwordEncoder.encode("secret"), "");
        when(repository.findByEmail("ssafy@example.com")).thenReturn(member);

        Member loggedIn = service.loginWithOAuth("google", "google-sub", "ssafy@example.com", "Google User");

        assertThat(loggedIn).isEqualTo(member);
        verify(repository).insertAuthLog("ssafy", "LOGIN");
        verify(repository, never()).insert(any());
    }

    @DisplayName("OAuth 로그인은 새 이메일이면 회원을 생성한다")
    @Test
    void oauthLoginCreatesMemberWhenEmailIsNew() {
        when(repository.findByEmail("new@example.com")).thenReturn(null);
        when(repository.existsByUserId("google_12345")).thenReturn(false);

        Member loggedIn = service.loginWithOAuth("google", "12345", "new@example.com", "New User");

        assertThat(loggedIn.userId()).isEqualTo("google_12345");
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(repository).insert(memberCaptor.capture());
        assertThat(memberCaptor.getValue().email()).isEqualTo("new@example.com");
        assertThat(passwordEncoder.matches("secret", memberCaptor.getValue().password())).isFalse();
        verify(repository).insertAuthLog("google_12345", "LOGIN");
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

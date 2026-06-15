package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.repository.MemberRepository;
import com.ssafy.enjoytrip.security.PasswordCodec;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberServiceTest {

    @DisplayName("회원가입은 비밀번호 인코딩 없이 중복 아이디나 이메일을 거부한다")
    @Test
    void signupRejectsDuplicateUserIdOrEmailWithoutEncodingPassword() {
        FakeMemberRepository repository = new FakeMemberRepository();
        repository.insert(member("dreamer", "dreamer@example.com", "encoded:old"));
        CountingPasswordCodec passwordCodec = new CountingPasswordCodec();
        MemberService service = new MemberService(repository, passwordCodec);

        CoreException exception = assertThrows(
                CoreException.class,
                () -> service.signup(member("dreamer", "other@example.com", "raw-password"))
        );

        assertEquals(ErrorType.USER_ALREADY_EXISTS, exception.errorType());
        assertEquals(0, passwordCodec.encodeCalls);
        assertEquals(1, repository.members.size());
    }

    @DisplayName("회원가입은 새 회원이면 인코딩된 비밀번호를 저장한다")
    @Test
    void signupStoresEncodedPasswordWhenMemberIsNew() {
        FakeMemberRepository repository = new FakeMemberRepository();
        CountingPasswordCodec passwordCodec = new CountingPasswordCodec();
        MemberService service = new MemberService(repository, passwordCodec);

        service.signup(member("newbie", "newbie@example.com", "raw-password"));

        assertEquals("encoded:raw-password", repository.findByUserId("newbie").password());
        assertEquals(1, passwordCodec.encodeCalls);
    }

    @DisplayName("로그인은 인증 실패 시 null을 반환하고 로그인 기록을 남기지 않는다")
    @Test
    void loginReturnsNullWhenPasswordDoesNotAuthenticateAndDoesNotWriteLog() {
        FakeMemberRepository repository = new FakeMemberRepository();
        repository.insert(member("traveler", "traveler@example.com", "encoded:correct"));
        MemberService service = new MemberService(repository, new CountingPasswordCodec());

        CoreException exception = assertThrows(CoreException.class, () -> service.login("traveler", "wrong"));

        assertEquals(ErrorType.INVALID_CREDENTIALS, exception.errorType());
        assertTrue(repository.authLogs.isEmpty());
    }

    @DisplayName("로그인은 기존 평문 비밀번호를 업그레이드하고 로그인 기록을 남긴다")
    @Test
    void loginUpgradesLegacyPlainPasswordAndRecordsLogin() {
        FakeMemberRepository repository = new FakeMemberRepository();
        repository.insert(member("legacy", "legacy@example.com", "plain-password"));
        CountingPasswordCodec passwordCodec = new CountingPasswordCodec();
        MemberService service = new MemberService(repository, passwordCodec);

        Member result = service.login("legacy", "plain-password");

        assertNotNull(result);
        assertEquals("encoded:plain-password", repository.findByUserId("legacy").password());
        assertEquals(List.of("legacy:LOGIN"), repository.authLogs);
        assertEquals(1, passwordCodec.encodeCalls);
    }

    @DisplayName("OAuth 로그인은 기존 이메일을 재사용하고 로그인 기록을 남긴다")
    @Test
    void loginWithOAuthReusesExistingEmailAndRecordsLogin() {
        FakeMemberRepository repository = new FakeMemberRepository();
        repository.insert(member("existing", "same@example.com", "encoded:any"));
        MemberService service = new MemberService(repository, new CountingPasswordCodec());

        Member result = service.loginWithOAuth("google", "provider-123", "same@example.com", "Google User");

        assertEquals("existing", result.userId());
        assertEquals(1, repository.members.size());
        assertEquals(List.of("existing:LOGIN"), repository.authLogs);
    }

    @DisplayName("OAuth 회원가입은 정규화된 provider 정보를 사용하고 로그인 기록을 남긴다")
    @Test
    void signupWithOAuthCreatesMemberAndRecordsLogin() {
        FakeMemberRepository repository = new FakeMemberRepository();
        CountingPasswordCodec passwordCodec = new CountingPasswordCodec();
        MemberService service = new MemberService(repository, passwordCodec);

        Member result = service.signupWithOAuth("Google", "provider-123", "google@example.com", "Google User");

        assertEquals("google_provider123", result.userId());
        assertEquals("Google User", result.name());
        assertEquals("google@example.com", result.email());
        assertTrue(result.password().startsWith("encoded:"));
        assertEquals(List.of("google_provider123:LOGIN"), repository.authLogs);
        assertEquals(1, passwordCodec.encodeCalls);
    }

    private static Member member(String userId, String email, String password) {
        return new Member(userId, userId, email, password, "");
    }

    private static final class CountingPasswordCodec implements PasswordCodec {
        private int encodeCalls;

        @Override
        public String encode(String rawPassword) {
            encodeCalls++;
            return "encoded:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            return ("encoded:" + rawPassword).equals(encodedPassword);
        }

        @Override
        public boolean isEncoded(String password) {
            return password != null && password.startsWith("encoded:");
        }
    }

    private static final class FakeMemberRepository implements MemberRepository {
        private final Map<String, Member> members = new HashMap<>();
        private final List<String> authLogs = new ArrayList<>();

        @Override
        public List<Member> findAll() {
            return new ArrayList<>(members.values());
        }

        @Override
        public Member findByUserId(String userId) {
            return members.get(userId);
        }

        @Override
        public Member findByEmail(String email) {
            return members.values().stream()
                    .filter(member -> email.equals(member.email()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public String findPassword(String userId, String email) {
            Member member = members.get(userId);
            if (member == null || !email.equals(member.email())) {
                return null;
            }
            return member.password();
        }

        @Override
        public boolean existsByUserId(String userId) {
            return members.containsKey(userId);
        }

        @Override
        public boolean existsByEmail(String email) {
            return findByEmail(email) != null;
        }

        @Override
        public void insert(Member member) {
            members.put(member.userId(), member);
        }

        @Override
        public boolean update(Member member) {
            if (!members.containsKey(member.userId())) {
                return false;
            }
            members.put(member.userId(), member);
            return true;
        }

        @Override
        public boolean delete(String userId) {
            return members.remove(userId) != null;
        }

        @Override
        public void insertAuthLog(String userId, String eventType) {
            authLogs.add(userId + ":" + eventType);
        }
    }
}

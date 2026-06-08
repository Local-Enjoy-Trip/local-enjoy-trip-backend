package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.repository.MemberRepository;
import com.ssafy.enjoytrip.security.PasswordCodec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberServiceTest {

    @Test
    void signupRejectsDuplicateUserIdOrEmailWithoutEncodingPassword() {
        FakeMemberRepository repository = new FakeMemberRepository();
        repository.insert(member("dreamer", "dreamer@example.com", "encoded:old"));
        CountingPasswordCodec passwordCodec = new CountingPasswordCodec();
        MemberService service = new MemberService(repository, passwordCodec);

        boolean result = service.signup(member("dreamer", "other@example.com", "raw-password"));

        assertFalse(result);
        assertEquals(0, passwordCodec.encodeCalls);
        assertEquals(1, repository.members.size());
    }

    @Test
    void signupStoresEncodedPasswordWhenMemberIsNew() {
        FakeMemberRepository repository = new FakeMemberRepository();
        CountingPasswordCodec passwordCodec = new CountingPasswordCodec();
        MemberService service = new MemberService(repository, passwordCodec);

        boolean result = service.signup(member("newbie", "newbie@example.com", "raw-password"));

        assertTrue(result);
        assertEquals("encoded:raw-password", repository.findByUserId("newbie").password());
        assertEquals(1, passwordCodec.encodeCalls);
    }

    @Test
    void loginReturnsNullWhenPasswordDoesNotAuthenticateAndDoesNotWriteLog() {
        FakeMemberRepository repository = new FakeMemberRepository();
        repository.insert(member("traveler", "traveler@example.com", "encoded:correct"));
        MemberService service = new MemberService(repository, new CountingPasswordCodec());

        Member result = service.login("traveler", "wrong");

        assertNull(result);
        assertTrue(repository.authLogs.isEmpty());
    }

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

    @Test
    void signupWithOAuthCreatesSanitizedMemberAndRecordsLogin() {
        FakeMemberRepository repository = new FakeMemberRepository();
        CountingPasswordCodec passwordCodec = new CountingPasswordCodec();
        MemberService service = new MemberService(repository, passwordCodec);

        Member result = service.signupWithOAuth("Google", "provider-123", "google@example.com", "");

        assertEquals("google_provider123", result.userId());
        assertEquals("google@example.com", result.name());
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

package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.repository.MemberRepository;
import com.ssafy.enjoytrip.security.PasswordCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository repository;
    private final PasswordCodec passwordCodec;

    public List<Member> findAllUsers() {
        return repository.findAll();
    }

    public Member findByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    public Member findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public boolean existsByUserId(String userId) {
        return repository.existsByUserId(userId);
    }

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    public boolean signup(Member member) {
        if (memberAlreadyExists(member)) {
            return false;
        }
        saveMemberWithEncodedPassword(member);
        return true;
    }

    public Member login(String userId, String password) {
        Member member = findAuthenticatableMember(userId, password);
        if (member == null) {
            return null;
        }

        upgradeLegacyPasswordIfNeeded(member, password);
        recordLogin(member);
        return member;
    }

    public Member loginWithOAuth(String provider, String providerUserId, String email, String name) {
        Member existing = repository.findByEmail(email);
        if (existing != null) {
            return loginExistingOAuthMember(existing);
        }

        return signupWithOAuth(provider, providerUserId, email, name);
    }

    public Member signupWithOAuth(String provider, String providerUserId, String email, String name) {
        Member existing = repository.findByEmail(email);
        if (existing != null) {
            return loginExistingOAuthMember(existing);
        }

        Member member = createOAuthMember(provider, providerUserId, email, name);
        saveOAuthMember(member);
        recordLogin(member);
        return member;
    }

    public void logout(String userId) {
        repository.insertAuthLog(userId, "LOGOUT");
    }

    public String findPassword(String userId, String email) {
        return repository.findPassword(userId, email);
    }

    public boolean update(Member member) {
        return repository.update(member.withEncodedPasswordWhenPresent(passwordCodec));
    }

    public boolean delete(String userId) {
        return repository.delete(userId);
    }

    private boolean memberAlreadyExists(Member member) {
        return repository.existsByUserId(member.userId()) || repository.existsByEmail(member.email());
    }

    private void saveMemberWithEncodedPassword(Member member) {
        repository.insert(member.withEncodedPassword(passwordCodec));
    }

    private Member findAuthenticatableMember(String userId, String password) {
        Member member = repository.findByUserId(userId);
        if (member == null || !member.canAuthenticate(password, passwordCodec)) {
            return null;
        }
        return member;
    }

    private void upgradeLegacyPasswordIfNeeded(Member member, String password) {
        if (member.shouldUpgradePassword(passwordCodec)) {
            repository.update(member.withPassword(passwordCodec.encode(password)));
        }
    }

    private Member loginExistingOAuthMember(Member member) {
        recordLogin(member);
        return member;
    }

    private Member createOAuthMember(String provider, String providerUserId, String email, String name) {
        return new Member(
                oauthUserId(provider, providerUserId),
                valueOrDefault(name, email),
                email,
                passwordCodec.encode(UUID.randomUUID().toString()),
                ""
        );
    }

    private void saveOAuthMember(Member member) {
        repository.insert(member);
    }

    private void recordLogin(Member member) {
        repository.insertAuthLog(member.userId(), "LOGIN");
    }

    private String oauthUserId(String provider, String providerUserId) {
        String normalizedProvider = valueOrDefault(provider, "oauth").toLowerCase(Locale.ROOT);
        String sourceId = valueOrDefault(providerUserId, UUID.randomUUID().toString());
        String normalizedId = sourceId.replaceAll("[^A-Za-z0-9_]", "");
        if (normalizedId.isBlank()) {
            normalizedId = Integer.toUnsignedString(sourceId.hashCode(), 36);
        }
        String userId = normalizedProvider + "_" + normalizedId;
        if (userId.length() <= 64) {
            return uniqueOauthUserId(userId, normalizedProvider);
        }
        return uniqueOauthUserId(
                normalizedProvider + "_" + Integer.toUnsignedString(sourceId.hashCode(), 36),
                normalizedProvider
        );
    }

    private String uniqueOauthUserId(String candidate, String provider) {
        if (!repository.existsByUserId(candidate)) {
            return candidate;
        }
        return provider + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    private static String valueOrDefault(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}

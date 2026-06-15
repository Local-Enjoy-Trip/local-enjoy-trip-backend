package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.support.error.ErrorType.EMAIL_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_CREDENTIALS;
import static com.ssafy.enjoytrip.support.error.ErrorType.USER_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.support.error.ErrorType.USER_NOT_FOUND;

import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.repository.MemberRepository;
import com.ssafy.enjoytrip.security.PasswordCodec;
import com.ssafy.enjoytrip.support.error.CoreException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public Member findRequiredByUserId(String userId) {
        Member member = repository.findByUserId(userId);
        if (member == null) {
            throw new CoreException(USER_NOT_FOUND);
        }
        return member;
    }

    public void signup(Member member) {
        validateNewMember(member);
        saveMemberWithEncodedPassword(member);
    }

    public Member login(String userId, String password) {
        Member member = findAuthenticatableMember(userId, password);
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

    public void update(Member member) {
        validateEmailOwner(member);
        if (!repository.update(member.withEncodedPasswordWhenPresent(passwordCodec))) {
            throw new CoreException(USER_NOT_FOUND);
        }
    }

    public void delete(String userId) {
        if (!repository.delete(userId)) {
            throw new CoreException(USER_NOT_FOUND);
        }
    }

    private void validateNewMember(Member member) {
        if (repository.existsByUserId(member.userId())) {
            throw new CoreException(USER_ALREADY_EXISTS);
        }
        if (repository.existsByEmail(member.email())) {
            throw new CoreException(EMAIL_ALREADY_EXISTS);
        }
    }

    private void validateEmailOwner(Member member) {
        if (member.email() == null) {
            return;
        }
        Member owner = repository.findByEmail(member.email());
        if (owner != null && !owner.userId().equals(member.userId())) {
            throw new CoreException(EMAIL_ALREADY_EXISTS);
        }
    }

    private void saveMemberWithEncodedPassword(Member member) {
        repository.insert(member.withEncodedPassword(passwordCodec));
    }

    private Member findAuthenticatableMember(String userId, String password) {
        Member member = repository.findByUserId(userId);
        if (member == null || !member.canAuthenticate(password, passwordCodec)) {
            throw new CoreException(INVALID_CREDENTIALS);
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
                name,
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
        String normalizedProvider = provider.toLowerCase(Locale.ROOT);
        String sourceId = providerUserId;
        String normalizedId = sourceId.replaceAll("[^A-Za-z0-9_]", "");
        if (normalizedId.isEmpty()) {
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

}

package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.EMAIL_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_CREDENTIALS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.entity.AuthLogEntity;
import com.ssafy.enjoytrip.storage.db.core.entity.MemberEntity;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AuthLogMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;
    private final AuthLogMapper authLogMapper;

    public List<Member> findAllUsers() {
        return memberMapper.findAllOrderByCreatedAtDesc().stream()
                .map(entity -> new Member(
                        entity.getUserId(),
                        entity.getName(),
                        entity.getNickname(),
                        entity.getEmail(),
                        entity.getPassword(),
                        entity.getProfileImageUrl(),
                        entity.getRepresentativeLatitude(),
                        entity.getRepresentativeLongitude(),
                        entity.getRepresentativeRegionName(),
                        stringValue(entity.getCreatedAt())
                ))
                .toList();
    }

    public Member findByUserId(String userId) {
        MemberEntity entity = memberMapper.findByUserId(userId);
        if (entity == null) {
            return null;
        }
        return new Member(
                entity.getUserId(),
                entity.getName(),
                entity.getNickname(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getProfileImageUrl(),
                entity.getRepresentativeLatitude(),
                entity.getRepresentativeLongitude(),
                entity.getRepresentativeRegionName(),
                stringValue(entity.getCreatedAt())
        );
    }

    public Member findByEmail(String email) {
        MemberEntity entity = memberMapper.findByEmail(email);
        if (entity == null) {
            return null;
        }
        return new Member(
                entity.getUserId(),
                entity.getName(),
                entity.getNickname(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getProfileImageUrl(),
                entity.getRepresentativeLatitude(),
                entity.getRepresentativeLongitude(),
                entity.getRepresentativeRegionName(),
                stringValue(entity.getCreatedAt())
        );
    }

    public Member findRequiredByUserId(String userId) {
        Member member = findByUserId(userId);
        if (member == null) {
            throw new CoreException(USER_NOT_FOUND);
        }
        return member;
    }

    @Transactional
    public void signup(Member member) {
        validateNewMember(member);
        saveMember(member.withPassword(passwordEncoder.encode(member.password())));
    }

    @Transactional
    public Member login(String userId, String password) {
        Member member = findAuthenticatableMember(userId, password);
        upgradeLegacyPasswordIfNeeded(member, password);
        authLogMapper.insert(new AuthLogEntity(member.userId(), "LOGIN"));
        return member;
    }

    @Transactional
    public Member loginWithOAuth(String provider, String providerUserId, String email, String name) {
        Member existing = findByEmail(email);
        if (existing != null) {
            authLogMapper.insert(new AuthLogEntity(existing.userId(), "LOGIN"));
            return existing;
        }

        Member member = createOAuthMember(provider, providerUserId, email, name);
        saveMember(member);
        authLogMapper.insert(new AuthLogEntity(member.userId(), "LOGIN"));
        return member;
    }

    @Transactional
    public Member signupWithOAuth(String provider, String providerUserId, String email, String name) {
        return loginWithOAuth(provider, providerUserId, email, name);
    }

    public void logout(String userId) {
        authLogMapper.insert(new AuthLogEntity(userId, "LOGOUT"));
    }

    public String findPassword(String userId, String email) {
        MemberEntity entity = memberMapper.findByUserIdAndEmail(userId, email);
        return entity == null ? null : entity.getPassword();
    }

    @Transactional
    public void update(Member member) {
        validateEmailOwner(member);
        if (!updateMemberEntity(member.withPassword(encodedPasswordWhenPresent(member.password())))) {
            throw new CoreException(USER_NOT_FOUND);
        }
    }

    @Transactional
    public void delete(String userId) {
        if (memberMapper.existsByUserId(userId) <= 0) {
            throw new CoreException(USER_NOT_FOUND);
        }
        memberMapper.deleteByUserId(userId);
    }

    private void validateNewMember(Member member) {
        if (memberMapper.existsByUserId(member.userId()) > 0) {
            throw new CoreException(USER_ALREADY_EXISTS);
        }
        if (memberMapper.existsByEmail(member.email()) > 0) {
            throw new CoreException(EMAIL_ALREADY_EXISTS);
        }
    }

    private void validateEmailOwner(Member member) {
        if (member.email() == null) {
            return;
        }
        Member owner = findByEmail(member.email());
        if (owner != null && !owner.userId().equals(member.userId())) {
            throw new CoreException(EMAIL_ALREADY_EXISTS);
        }
    }

    private Member findAuthenticatableMember(String userId, String password) {
        Member member = findByUserId(userId);
        if (member == null || !matchesPassword(password, member.password())) {
            throw new CoreException(INVALID_CREDENTIALS);
        }
        return member;
    }

    private void upgradeLegacyPasswordIfNeeded(Member member, String password) {
        if (shouldUpgradePassword(member.password())) {
            updateMemberEntity(member.withPassword(passwordEncoder.encode(password)));
        }
    }

    private String encodedPasswordWhenPresent(String password) {
        if (isBlank(password)) {
            return password;
        }
        return passwordEncoder.encode(password);
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (isBlank(rawPassword) || isBlank(storedPassword)) {
            return false;
        }
        if (isEncodedPassword(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    private boolean shouldUpgradePassword(String password) {
        return !isBlank(password) && !isEncodedPassword(password);
    }

    private boolean isEncodedPassword(String password) {
        return password != null && password.startsWith("$2");
    }

    private Member createOAuthMember(String provider, String providerUserId, String email, String name) {
        return new Member(
                oauthUserId(provider, providerUserId),
                name,
                email,
                passwordEncoder.encode(UUID.randomUUID().toString()),
                ""
        );
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
        if (memberMapper.existsByUserId(candidate) <= 0) {
            return candidate;
        }
        return provider + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    private void saveMember(Member member) {
        memberMapper.insert(new MemberEntity(
                member.userId(),
                member.name(),
                member.nickname(),
                member.email(),
                member.password(),
                member.profileImageUrl(),
                member.representativeLatitude(),
                member.representativeLongitude(),
                member.representativeRegionName()
        ));
    }

    private boolean updateMemberEntity(Member member) {
        MemberEntity entity = memberMapper.findByUserId(member.userId());
        if (entity == null) {
            return false;
        }
        entity.update(
                member.name(),
                member.nickname(),
                member.email(),
                member.password(),
                member.profileImageUrl(),
                member.representativeLatitude(),
                member.representativeLongitude(),
                member.representativeRegionName()
        );
        return memberMapper.update(entity) > 0;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}

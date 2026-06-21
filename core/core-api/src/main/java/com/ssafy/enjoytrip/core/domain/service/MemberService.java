package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.EMAIL_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_CREDENTIALS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.AuthLogRecord;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
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
                .map(record -> new Member(
                        record.getUserId(),
                        record.getName(),
                        record.getNickname(),
                        record.getEmail(),
                        record.getPassword(),
                        record.getProfileImageUrl(),
                        record.getRepresentativeLatitude(),
                        record.getRepresentativeLongitude(),
                        record.getRepresentativeRegionName(),
                        stringValue(record.getCreatedAt())
                ))
                .toList();
    }

    public Member findByUserId(String userId) {
        MemberRecord record = memberMapper.findByUserId(userId);
        if (record == null) {
            return null;
        }
        return new Member(
                record.getUserId(),
                record.getName(),
                record.getNickname(),
                record.getEmail(),
                record.getPassword(),
                record.getProfileImageUrl(),
                record.getRepresentativeLatitude(),
                record.getRepresentativeLongitude(),
                record.getRepresentativeRegionName(),
                stringValue(record.getCreatedAt())
        );
    }

    public Member findByEmail(String email) {
        MemberRecord record = memberMapper.findByEmail(email);
        if (record == null) {
            return null;
        }
        return new Member(
                record.getUserId(),
                record.getName(),
                record.getNickname(),
                record.getEmail(),
                record.getPassword(),
                record.getProfileImageUrl(),
                record.getRepresentativeLatitude(),
                record.getRepresentativeLongitude(),
                record.getRepresentativeRegionName(),
                stringValue(record.getCreatedAt())
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
        authLogMapper.insert(new AuthLogRecord(member.userId(), "LOGIN"));
        return member;
    }

    @Transactional
    public Member loginWithOAuth(String provider, String providerUserId, String email, String name) {
        return loginOrCreateOAuthMember(provider, providerUserId, email, name, name);
    }

    @Transactional
    public Member signupWithOAuth(String provider,
                                  String providerUserId,
                                  String email,
                                  String name,
                                  String nickname) {
        return loginOrCreateOAuthMember(provider, providerUserId, email, name, nickname);
    }

    public void logout(String userId) {
        authLogMapper.insert(new AuthLogRecord(userId, "LOGOUT"));
    }

    @Transactional
    public void update(Member member) {
        MemberRecord record = memberMapper.findByUserId(member.userId());
        if (record == null) {
            throw new CoreException(USER_NOT_FOUND);
        }
        record.update(
                member.nickname(),
                member.profileImageUrl(),
                member.representativeLatitude(),
                member.representativeLongitude(),
                member.representativeRegionName()
        );
        memberMapper.update(record);
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
        if (member == null || !passwordEncoder.matches(password, member.password())) {
            throw new CoreException(INVALID_CREDENTIALS);
        }
        return member;
    }

    private Member createOAuthMember(String provider,
                                     String providerUserId,
                                     String email,
                                     String name,
                                     String nickname) {
        return new Member(
                oauthUserId(provider, providerUserId),
                name,
                nickname,
                email,
                passwordEncoder.encode(UUID.randomUUID().toString()),
                null,
                null,
                null,
                null,
                ""
        );
    }

    private Member loginOrCreateOAuthMember(String provider,
                                            String providerUserId,
                                            String email,
                                            String name,
                                            String nickname) {
        Member existing = findByEmail(email);
        if (existing != null) {
            authLogMapper.insert(new AuthLogRecord(existing.userId(), "LOGIN"));
            return existing;
        }

        Member member = createOAuthMember(provider, providerUserId, email, name, nickname);
        saveMember(member);
        authLogMapper.insert(new AuthLogRecord(member.userId(), "LOGIN"));
        return member;
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
        memberMapper.insert(new MemberRecord(
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

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}

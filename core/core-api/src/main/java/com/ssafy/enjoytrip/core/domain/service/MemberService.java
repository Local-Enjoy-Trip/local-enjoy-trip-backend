package com.ssafy.enjoytrip.core.domain.service;

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
                .map(MemberService::toMember)
                .toList();
    }

    public List<Member> searchByEmail(String emailKeyword) {
        return memberMapper.findByEmailKeyword(emailKeyword).stream()
                .map(MemberService::toMember)
                .toList();
    }

    public Member findById(Long memberId) {
        MemberRecord record = memberMapper.findById(memberId);
        if (record == null) {
            return null;
        }
        return toMember(record);
    }

    public Member findByEmail(String email) {
        MemberRecord record = memberMapper.findByEmail(email);
        if (record == null) {
            return null;
        }
        return toMember(record);
    }

    public Member findRequiredById(Long memberId) {
        Member member = findById(memberId);
        if (member == null) {
            throw new CoreException(USER_NOT_FOUND);
        }
        return member;
    }

    public boolean isEmailAvailable(String email) {
        return memberMapper.existsByEmail(email) == 0;
    }

    @Transactional
    public void signup(Member member) {
        validateNewMember(member);
        saveMember(member.withPassword(passwordEncoder.encode(member.password())));
    }

    @Transactional
    public Member login(String email, String password) {
        Member member = findAuthenticatableMember(email, password);
        authLogMapper.insert(new AuthLogRecord(member.memberId(), "LOGIN"));
        return member;
    }

    @Transactional
    public Member loginWithOAuth(String provider, String providerSubject, String email, String name) {
        Member existing = findByEmail(email);
        if (existing != null) {
            authLogMapper.insert(new AuthLogRecord(existing.memberId(), "LOGIN"));
            return existing;
        }

        Member member = saveMember(createOAuthMember(provider, providerSubject, email, name, name));
        authLogMapper.insert(new AuthLogRecord(member.memberId(), "LOGIN"));
        return member;
    }

    @Transactional
    public Member signupWithOAuth(String provider,
                                  String providerSubject,
                                  String email,
                                  String name,
                                  String nickname) {
        Member member = createOAuthMember(provider, providerSubject, email, name, nickname);
        validateNewMember(member);
        Member saved = saveMember(member);
        authLogMapper.insert(new AuthLogRecord(saved.memberId(), "LOGIN"));
        return saved;
    }

    public void logout(Long memberId) {
        authLogMapper.insert(new AuthLogRecord(memberId, "LOGOUT"));
    }

    @Transactional
    public void update(Member member) {
        MemberRecord record = memberMapper.findById(member.memberId());
        if (record == null) {
            throw new CoreException(USER_NOT_FOUND);
        }
        record.updateNickname(member.nickname());
        memberMapper.update(record);
    }

    @Transactional
    public void delete(Long memberId) {
        if (memberMapper.deleteById(memberId) <= 0) {
            throw new CoreException(USER_NOT_FOUND);
        }
    }

    private void validateNewMember(Member member) {
        if (memberMapper.existsByEmail(member.email()) > 0) {
            throw new CoreException(USER_ALREADY_EXISTS);
        }
    }

    private Member findAuthenticatableMember(String email, String password) {
        Member member = findByEmail(email);
        if (member == null || !passwordEncoder.matches(password, member.password())) {
            throw new CoreException(INVALID_CREDENTIALS);
        }
        return member;
    }

    private Member createOAuthMember(String provider,
                                     String providerSubject,
                                     String email,
                                     String name,
                                     String nickname) {
        return new Member(
                null,
                name,
                nickname,
                email,
                passwordEncoder.encode(provider + ":" + providerSubject + ":" + UUID.randomUUID()),
                null
        );
    }

    private Member saveMember(Member member) {
        MemberRecord record = new MemberRecord(
                member.name(),
                member.nickname(),
                member.email(),
                member.password(),
                member.profileImageUrl()
        );
        memberMapper.insert(record);
        return member.withMemberId(record.getId());
    }

    private static Member toMember(MemberRecord record) {
        return new Member(
                record.getId(),
                record.getName(),
                record.getNickname(),
                record.getEmail(),
                record.getPassword(),
                record.getProfileImageUrl()
        );
    }
}

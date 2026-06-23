package com.ssafy.enjoytrip.core.api.security;

import com.ssafy.enjoytrip.core.domain.MemberRole;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAuthenticationSupport {
    private final MemberMapper memberMapper;

    public boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        MemberRecord member = findAuthenticatedMember(authentication);
        return isAdminRole(member);
    }

    public String requireAdminUserId(Authentication authentication) {
        MemberRecord member = findAuthenticatedMember(authentication);
        if (!isAdminRole(member)) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }
        return member.getUserId();
    }

    private static boolean isAdminRole(MemberRecord member) {
        return member != null && MemberRole.ADMIN.name().equals(member.getRole());
    }

    private MemberRecord findAuthenticatedMember(Authentication authentication) {
        return memberMapper.findByUserId(authentication.getName());
    }
}

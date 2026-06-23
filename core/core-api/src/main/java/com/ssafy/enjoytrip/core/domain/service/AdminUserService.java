package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.MemberRole;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final MemberMapper memberMapper;

    public List<AdminUserSummary> findUsers() {
        return memberMapper.findAllOrderByCreatedAtDesc().stream()
                .map(AdminUserService::toSummary)
                .toList();
    }

    public long countAdmins(List<AdminUserSummary> users) {
        return users.stream()
                .filter(AdminUserSummary::admin)
                .count();
    }

    private static AdminUserSummary toSummary(MemberRecord record) {
        return new AdminUserSummary(
                record.getId(),
                displayName(record),
                record.getEmail(),
                role(record.getRole()),
                MemberRole.ADMIN.name().equals(role(record.getRole())),
                stringValue(record.getCreatedAt())
        );
    }

    private static String displayName(MemberRecord record) {
        if (!isBlank(record.getNickname())) {
            return record.getNickname();
        }
        if (!isBlank(record.getName())) {
            return record.getName();
        }
        return record.getEmail();
    }

    private static String role(String role) {
        if (isBlank(role)) {
            return "USER";
        }
        return role;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String stringValue(LocalDateTime value) {
        return value == null ? "" : value.toString();
    }

    public record AdminUserSummary(
            Long memberId,
            String displayName,
            String email,
            String role,
            boolean admin,
            String createdAt
    ) {
    }
}

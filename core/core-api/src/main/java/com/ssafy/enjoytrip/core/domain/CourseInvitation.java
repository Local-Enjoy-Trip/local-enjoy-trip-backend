package com.ssafy.enjoytrip.core.domain;

import java.time.LocalDateTime;

public record CourseInvitation(
        Long id,
        String courseId,
        Long inviterMemberId,
        Long inviteeMemberId,
        CourseInvitationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

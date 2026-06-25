package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.CourseInvitation;
import com.ssafy.enjoytrip.core.domain.CourseInvitationStatus;
import java.time.LocalDateTime;

public record CourseInvitationResponse(
        Long id,
        String courseId,
        Long inviterMemberId,
        Long inviteeMemberId,
        CourseInvitationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CourseInvitationResponse from(CourseInvitation invitation) {
        return new CourseInvitationResponse(
                invitation.id(),
                invitation.courseId(),
                invitation.inviterMemberId(),
                invitation.inviteeMemberId(),
                invitation.status(),
                invitation.createdAt(),
                invitation.updatedAt()
        );
    }
}

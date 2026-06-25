package com.ssafy.enjoytrip.core.domain.event;

public record CourseInvitationSentEvent(
        Long invitationId,
        String courseId,
        String courseTitle,
        Long inviterMemberId,
        Long inviteeMemberId
) {
}

package com.ssafy.enjoytrip.core.api.web.dto.response;

import java.util.List;

public record CourseInvitationsResponse(
        List<CourseInvitationResponse> invitations
) {
}

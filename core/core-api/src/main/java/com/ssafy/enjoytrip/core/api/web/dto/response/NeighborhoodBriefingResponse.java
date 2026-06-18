package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.NeighborhoodBriefing;

public record NeighborhoodBriefingResponse(
        String region,
        String briefing
) {
    public NeighborhoodBriefingResponse(NeighborhoodBriefing briefing) {
        this(briefing.region(), briefing.briefing());
    }
}

package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.NeighborhoodBriefing;

public record NeighborhoodBriefingResponse(
        String region,
        String briefing
) {
    public NeighborhoodBriefingResponse(NeighborhoodBriefing briefing) {
        this(briefing.region(), briefing.briefing());
    }
}

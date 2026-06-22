package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NeighborhoodBriefingRequest(
        @NotBlank String regionName,
        Double latitude,
        Double longitude
) {
    public String toRegionName() {
        return regionName.strip();
    }
}


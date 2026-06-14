package com.ssafy.enjoytrip.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NeighborhoodBriefingRequest(
        @NotBlank String regionName
) {
    public String toRegionName() {
        return regionName.strip();
    }
}

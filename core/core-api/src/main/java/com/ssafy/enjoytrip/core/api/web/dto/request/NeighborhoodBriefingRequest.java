package com.ssafy.enjoytrip.core.api.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record NeighborhoodBriefingRequest(
        @Schema(description = "브리핑 기준 지역명", example = "서울 중구")
        @NotBlank String regionName,

        @Schema(description = "브리핑 기준 위도", example = "37.5665")
        Double latitude,

        @Schema(description = "브리핑 기준 경도", example = "126.9780")
        Double longitude
) {
    public String toRegionName() {
        return regionName.strip();
    }
}

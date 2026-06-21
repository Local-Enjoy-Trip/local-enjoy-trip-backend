package com.ssafy.enjoytrip.core.api.web.dto.response;

public record UserResponse(
        String userId,
        String name,
        String nickname,
        String email,
        String profileImageUrl,
        Double representativeLatitude,
        Double representativeLongitude,
        String representativeRegionName
) {
}

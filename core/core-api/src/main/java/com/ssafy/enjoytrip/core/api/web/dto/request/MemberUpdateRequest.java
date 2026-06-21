package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
        @Size(min = 2, max = 30)
        String nickname,

        @Size(max = 512)
        String profileImageUrl,

        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double representativeLatitude,

        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double representativeLongitude,

        @Size(max = 100)
    String representativeRegionName
) {
    public String normalizedNickname() {
        return trimToNull(nickname);
    }

    public String normalizedProfileImageUrl() {
        return trimToNull(profileImageUrl);
    }

    public String normalizedRepresentativeRegionName() {
        return trimToNull(representativeRegionName);
    }

    @AssertTrue
    boolean isRepresentativeLocationComplete() {
        return (representativeLatitude == null) == (representativeLongitude == null);
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

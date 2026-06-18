package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberSignupRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_]{4,20}$")
        String userId,

        @NotBlank
        @Size(min = 2, max = 30)
        String name,

        @Size(min = 2, max = 30)
        String nickname,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$")
        String password,

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
    public String nicknameOrName() {
        String normalizedNickname = trimToNull(nickname);
        if (normalizedNickname == null) {
            return name.trim();
        }
        return normalizedNickname;
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

package com.ssafy.enjoytrip.web.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
        @Size(min = 2, max = 30)
        String name,

        @Size(min = 2, max = 30)
        String nickname,

        @Email
        String email,

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
    public String normalizedName() {
        return trimToEmpty(name);
    }

    public String normalizedNickname() {
        return trimToEmpty(nickname);
    }

    public String normalizedEmail() {
        return trimToEmpty(email);
    }

    public String normalizedPassword() {
        return trimToEmpty(password);
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

    private static String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

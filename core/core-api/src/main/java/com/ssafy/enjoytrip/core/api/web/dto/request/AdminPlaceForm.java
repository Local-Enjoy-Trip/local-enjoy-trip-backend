package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Locale;

public record AdminPlaceForm(
        @NotNull Long id,
        @NotBlank String title,
        String addr1,
        String addr2,
        String firstImage,
        String firstImage2,
        Integer sidoCode,
        Integer gugunCode,
        @NotNull Double latitude,
        @NotNull Double longitude,
        String contentTypeId,
        String overview,
        String status,
        Long duplicateOfAttractionId,
        String duplicateReason
) {
    public String normalizedTitle() {
        return title.strip();
    }

    public String normalizedStatus() {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        return status.strip().toUpperCase(Locale.ROOT);
    }

    public String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }
}

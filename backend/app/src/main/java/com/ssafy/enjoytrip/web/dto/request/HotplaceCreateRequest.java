package com.ssafy.enjoytrip.web.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HotplaceCreateRequest(
        @NotBlank String id,
        @NotBlank String userId,
        @NotBlank String title,
        @NotBlank String type,
        @NotBlank String visitDate,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double lat,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double lng,
        String description,
        String photo
) {
    public String normalizedId() {
        return id.strip();
    }

    public String normalizedUserId() {
        return userId.strip();
    }

    public String normalizedTitle() {
        return title.strip();
    }

    public String normalizedType() {
        return type.strip();
    }

    public String normalizedVisitDate() {
        return visitDate.strip();
    }

    public String normalizedDescription() {
        return stripToEmpty(description);
    }

    public String normalizedPhoto() {
        return stripToEmpty(photo);
    }

    private static String stripToEmpty(String value) {
        if (value == null) {
            return "";
        }

        return value.strip();
    }
}

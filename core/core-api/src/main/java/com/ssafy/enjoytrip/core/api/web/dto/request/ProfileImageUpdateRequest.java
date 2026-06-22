package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.support.error.exception.ClientInputException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record ProfileImageUpdateRequest(
        @NotBlank @Size(max = 512) String objectKey,
        @NotBlank @Size(max = 100) @Pattern(regexp = "image/.+") String contentType
) {
    private static final String INVALID_OBJECT_KEY_MESSAGE = "프로필 이미지 경로가 올바르지 않습니다.";
    private static final String PROFILE_IMAGE_FILE_NAME_PATTERN =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}"
                    + "-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.[A-Za-z0-9]{1,10}$";

    public String normalizedObjectKeyFor(String authenticatedUserId) {
        String normalized = objectKey.strip();
        if (!matchesProfileImageObjectKey(authenticatedUserId, normalized)) {
            throw new ClientInputException(INVALID_OBJECT_KEY_MESSAGE);
        }
        return normalized;
    }

    public String normalizedContentType() {
        return contentType.strip().toLowerCase(Locale.ROOT);
    }

    private static boolean matchesProfileImageObjectKey(String authenticatedUserId, String objectKey) {
        String requiredPrefix = "profiles/" + authenticatedUserId + "/";
        if (!objectKey.startsWith(requiredPrefix)) {
            return false;
        }
        return objectKey.substring(requiredPrefix.length()).matches(PROFILE_IMAGE_FILE_NAME_PATTERN);
    }
}

package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.ProfileImageUploadUrl;
import java.time.Instant;

public record ProfileImagePresignedUploadResponse(
        String objectKey,
        String uploadUrl,
        Instant expiresAt,
        String publicUrl
) {
    public static ProfileImagePresignedUploadResponse from(ProfileImageUploadUrl uploadUrl) {
        return new ProfileImagePresignedUploadResponse(
                uploadUrl.objectKey(),
                uploadUrl.uploadUrl(),
                uploadUrl.expiresAt(),
                uploadUrl.publicUrl()
        );
    }
}

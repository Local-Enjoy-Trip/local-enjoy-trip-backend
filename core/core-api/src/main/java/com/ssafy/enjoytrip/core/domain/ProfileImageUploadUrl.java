package com.ssafy.enjoytrip.core.domain;

import java.time.Instant;

public record ProfileImageUploadUrl(
        String objectKey,
        String uploadUrl,
        Instant expiresAt,
        String publicUrl
) {
}

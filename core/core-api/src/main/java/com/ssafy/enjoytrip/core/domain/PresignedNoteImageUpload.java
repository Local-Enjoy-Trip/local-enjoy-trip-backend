package com.ssafy.enjoytrip.core.domain;

import java.time.Instant;

public record PresignedNoteImageUpload(
        String objectKey,
        String uploadUrl,
        Instant expiresAt,
        String publicUrl
) {
}

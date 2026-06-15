package com.ssafy.enjoytrip.domain;

import java.time.Instant;

public record PresignedNoteImageUpload(
        String objectKey,
        String uploadUrl,
        Instant expiresAt,
        String publicUrl
) {
}

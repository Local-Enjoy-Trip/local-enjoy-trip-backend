package com.ssafy.enjoytrip.core.domain;

import java.time.Instant;

public record NoteImageUploadUrl(
        String objectKey,
        String uploadUrl,
        Instant expiresAt,
        String publicUrl
) {
}

package com.ssafy.enjoytrip.external.minio;

import java.time.Instant;

public record NoteImageUploadResult(
        String objectKey,
        String uploadUrl,
        Instant expiresAt,
        String publicUrl
) {
}

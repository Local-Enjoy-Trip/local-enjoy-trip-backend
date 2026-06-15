package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.NoteImageUploadUrl;
import java.time.Instant;

public record NoteImagePresignedUploadResponse(
        String objectKey,
        String uploadUrl,
        Instant expiresAt,
        String publicUrl
) {
    public static NoteImagePresignedUploadResponse from(NoteImageUploadUrl uploadUrl) {
        return new NoteImagePresignedUploadResponse(
                uploadUrl.objectKey(),
                uploadUrl.uploadUrl(),
                uploadUrl.expiresAt(),
                uploadUrl.publicUrl()
        );
    }
}

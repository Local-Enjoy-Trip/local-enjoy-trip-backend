package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.NoteImageUploadUrl;
import com.ssafy.enjoytrip.external.minio.MinioNoteImageUploadUrlGenerator;
import com.ssafy.enjoytrip.external.minio.NoteImageUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteImageUploadService {
    private final MinioNoteImageUploadUrlGenerator uploadUrlGenerator;

    public NoteImageUploadUrl createPresignedUpload(
            Long memberId,
            String contentType,
            String fileExtension
    ) {
        NoteImageUploadResult generated = uploadUrlGenerator.generate(
                String.valueOf(memberId),
                contentType,
                fileExtension
        );

        return new NoteImageUploadUrl(
                generated.objectKey(),
                generated.uploadUrl(),
                generated.expiresAt(),
                generated.publicUrl()
        );
    }
}

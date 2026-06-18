package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.NoteImageUploadUrl;
import com.ssafy.enjoytrip.core.domain.external.minio.NoteImageUploadUrlGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteImageService {
    private final NoteImageUploadUrlGenerator uploadUrlGenerator;

    public NoteImageUploadUrl createUploadUrl(
            String userId,
            String contentType,
            String fileExtension
    ) {
        return uploadUrlGenerator.generate(userId, contentType, fileExtension);
    }
}

package com.ssafy.enjoytrip.core.domain.external.minio;

import com.ssafy.enjoytrip.core.domain.NoteImageUploadUrl;

public interface NoteImageUploadUrlGenerator {
    NoteImageUploadUrl generate(String userId, String contentType, String fileExtension);
}

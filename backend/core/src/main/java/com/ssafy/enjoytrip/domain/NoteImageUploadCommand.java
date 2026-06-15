package com.ssafy.enjoytrip.domain;

public record NoteImageUploadCommand(
        String userId,
        String contentType,
        String fileExtension
) {
}

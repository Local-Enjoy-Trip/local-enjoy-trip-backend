package com.ssafy.enjoytrip.domain;

public record CreateNoteImageUploadCommand(
        String userId,
        String contentType,
        String fileExtension
) {
}

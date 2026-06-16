package com.ssafy.enjoytrip.application.dto.command;

public record NoteImageUploadCommand(
        String userId,
        String contentType,
        String fileExtension
) {
}

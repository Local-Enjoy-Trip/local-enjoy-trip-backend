package com.ssafy.enjoytrip.dto.command;

public record NoteImageUploadCommand(
        String userId,
        String contentType,
        String fileExtension
) {
}

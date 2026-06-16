package com.ssafy.enjoytrip.web.dto.request;

import com.ssafy.enjoytrip.application.dto.command.NoteImageUploadCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record NoteImagePresignedUploadRequest(
        @NotBlank @Size(max = 100) @Pattern(regexp = "image/.+") String contentType,
        @NotBlank @Size(max = 10) @Pattern(regexp = "\\.?[A-Za-z0-9]+") String fileExtension
) {
    public NoteImageUploadCommand toCommand(String userId) {
        return new NoteImageUploadCommand(
                userId,
                contentType.strip().toLowerCase(Locale.ROOT),
                normalizedFileExtension()
        );
    }

    private String normalizedFileExtension() {
        String normalized = fileExtension.strip().toLowerCase(Locale.ROOT);
        if (normalized.startsWith(".")) {
            return normalized.substring(1);
        }
        return normalized;
    }
}

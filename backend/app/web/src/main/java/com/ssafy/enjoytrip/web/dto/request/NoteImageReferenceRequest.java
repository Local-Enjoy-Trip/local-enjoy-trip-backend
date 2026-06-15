package com.ssafy.enjoytrip.web.dto.request;

import com.ssafy.enjoytrip.domain.NoteImageReference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NoteImageReferenceRequest(
        @NotBlank @Size(max = 512) String objectKey,
        @Size(max = 1024) String publicUrl,
        @NotBlank @Size(max = 100) @Pattern(regexp = "image/.+") String contentType
) {
    public NoteImageReference toReference() {
        return new NoteImageReference(
                objectKey.strip(),
                blankToNull(publicUrl),
                contentType.strip().toLowerCase()
        );
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }
}

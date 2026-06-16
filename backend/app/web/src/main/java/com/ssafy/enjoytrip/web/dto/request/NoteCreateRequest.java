package com.ssafy.enjoytrip.web.dto.request;

import com.ssafy.enjoytrip.dto.command.CreateNoteCommand;
import com.ssafy.enjoytrip.domain.NoteCategory;
import com.ssafy.enjoytrip.domain.NoteVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NoteCreateRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank String content,
        @NotNull NoteCategory category,
        @NotNull NoteVisibility visibility,
        @NotNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double latitude,
        @NotNull @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double longitude,
        @Size(max = 100) String regionName,
        @Valid NoteImageReferenceRequest image
) {
    public CreateNoteCommand toCommand(String authorUserId) {
        return new CreateNoteCommand(
                authorUserId,
                title.strip(),
                content.strip(),
                category,
                visibility,
                latitude,
                longitude,
                blankToNull(regionName),
                image == null ? null : image.toReference()
        );
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.strip();
    }
}

package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.Note;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NoteImageReference;
import com.ssafy.enjoytrip.core.domain.NoteStatus;
import com.ssafy.enjoytrip.core.domain.NoteVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NoteUpdateRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank String content,
        @NotNull NoteCategory category,
        @NotNull NoteVisibility visibility,
        @NotNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double latitude,
        @NotNull @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double longitude,
        @Size(max = 100) String regionName,
        @Valid NoteImageReferenceRequest image
) {
    public Note toNote(Long id, Long authorMemberId) {
        NoteImageReference imageReference = image == null ? null : image.toReference();

        return new Note(
                id,
                authorMemberId,
                title.strip(),
                content.strip(),
                category,
                visibility,
                latitude,
                longitude,
                blankToNull(regionName),
                imageObjectKey(imageReference),
                imagePublicUrl(imageReference),
                imageContentType(imageReference),
                NoteStatus.ACTIVE,
                null,
                null,
                null
        );
    }

    private static String imageObjectKey(NoteImageReference imageReference) {
        return imageReference == null ? null : imageReference.objectKey();
    }

    private static String imagePublicUrl(NoteImageReference imageReference) {
        return imageReference == null ? null : imageReference.publicUrl();
    }

    private static String imageContentType(NoteImageReference imageReference) {
        return imageReference == null ? null : imageReference.contentType();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.strip();
    }
}

package com.ssafy.enjoytrip.core.domain;

import static com.ssafy.enjoytrip.core.domain.NoteStatus.ACTIVE;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.NOTE_ACCESS_DENIED;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.NOTE_NOT_ACTIVE;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import java.time.LocalDateTime;

public record Note(
        Long id,
        Long authorMemberId,
        String title,
        String content,
        NoteCategory category,
        NoteVisibility visibility,
        double latitude,
        double longitude,
        String regionName,
        String imageObjectKey,
        String imageUrl,
        String imageContentType,
        NoteStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
    public void requireEditableBy(Long authorMemberId) {
        requireActive();
        requireAuthor(authorMemberId);
    }

    private void requireActive() {
        if (status != ACTIVE) {
            throw new CoreException(NOTE_NOT_ACTIVE);
        }
    }

    private void requireAuthor(Long authorMemberId) {
        if (!this.authorMemberId.equals(authorMemberId)) {
            throw new CoreException(NOTE_ACCESS_DENIED);
        }
    }
}

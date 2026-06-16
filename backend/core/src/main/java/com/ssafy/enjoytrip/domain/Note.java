package com.ssafy.enjoytrip.domain;

import static com.ssafy.enjoytrip.domain.NoteStatus.ACTIVE;
import static com.ssafy.enjoytrip.support.error.ErrorType.NOTE_ACCESS_DENIED;
import static com.ssafy.enjoytrip.support.error.ErrorType.NOTE_NOT_ACTIVE;

import com.ssafy.enjoytrip.support.error.CoreException;
import java.time.LocalDateTime;

public record Note(
        Long id,
        String authorUserId,
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
    public void requireEditableBy(String authorUserId) {
        requireActive();
        requireAuthor(authorUserId);
    }

    private void requireActive() {
        if (status != ACTIVE) {
            throw new CoreException(NOTE_NOT_ACTIVE);
        }
    }

    private void requireAuthor(String authorUserId) {
        if (!this.authorUserId.equals(authorUserId)) {
            throw new CoreException(NOTE_ACCESS_DENIED);
        }
    }
}

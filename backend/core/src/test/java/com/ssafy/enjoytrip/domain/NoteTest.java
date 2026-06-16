package com.ssafy.enjoytrip.domain;

import static com.ssafy.enjoytrip.support.error.ErrorType.NOTE_ACCESS_DENIED;
import static com.ssafy.enjoytrip.support.error.ErrorType.NOTE_NOT_ACTIVE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ssafy.enjoytrip.support.error.CoreException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoteTest {

    @DisplayName("활성 쪽지는 작성자 본인일 때 수정 가능하다")
    @Test
    void requireEditableByAllowsActiveAuthor() {
        Note note = note(NoteStatus.ACTIVE, "author");

        assertDoesNotThrow(() -> note.requireEditableBy("author"));
    }

    @DisplayName("비활성 쪽지는 작성자 확인보다 먼저 활성 상태 예외를 발생시킨다")
    @Test
    void requireEditableByChecksStatusBeforeAuthor() {
        Note note = note(NoteStatus.DELETED, "author");

        CoreException exception = assertThrows(
                CoreException.class,
                () -> note.requireEditableBy("other")
        );

        assertEquals(NOTE_NOT_ACTIVE, exception.errorType());
    }

    @DisplayName("활성 쪽지는 작성자가 다르면 접근 거부 예외를 발생시킨다")
    @Test
    void requireEditableByRejectsDifferentAuthor() {
        Note note = note(NoteStatus.ACTIVE, "author");

        CoreException exception = assertThrows(
                CoreException.class,
                () -> note.requireEditableBy("other")
        );

        assertEquals(NOTE_ACCESS_DENIED, exception.errorType());
    }

    private static Note note(NoteStatus status, String authorUserId) {
        return new Note(
                1L,
                authorUserId,
                "서울 산책",
                "content",
                NoteCategory.TIP,
                NoteVisibility.PUBLIC,
                37.5665,
                126.9780,
                "서울",
                null,
                null,
                null,
                status,
                LocalDateTime.of(2026, 6, 10, 10, 0),
                null,
                null
        );
    }
}

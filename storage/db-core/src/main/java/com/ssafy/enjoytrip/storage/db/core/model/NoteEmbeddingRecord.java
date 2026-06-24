package com.ssafy.enjoytrip.storage.db.core.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoteEmbeddingRecord {
    private Long id;
    private Long noteId;
    private String status;
    private String failureCode;
    private String failureMessage;
    private Integer attemptCount;
    private LocalDateTime lastAttemptedAt;
    private LocalDateTime embeddedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

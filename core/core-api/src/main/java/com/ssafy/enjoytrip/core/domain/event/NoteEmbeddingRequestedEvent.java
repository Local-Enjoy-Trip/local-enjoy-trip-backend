package com.ssafy.enjoytrip.core.domain.event;

public record NoteEmbeddingRequestedEvent(Long noteId, String content) {}

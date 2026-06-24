package com.ssafy.enjoytrip.external.embedding;

public class NoteEmbeddingException extends RuntimeException {
    private final String failureCode;

    public NoteEmbeddingException(String failureCode, String message) {
        super(message);
        this.failureCode = failureCode;
    }

    public NoteEmbeddingException(String failureCode, String message, Throwable cause) {
        super(message, cause);
        this.failureCode = failureCode;
    }

    public String failureCode() {
        return failureCode;
    }
}

package com.ssafy.enjoytrip.external.profile;

public class MemberProfileEmbeddingException extends RuntimeException {
    private final String failureCode;

    public MemberProfileEmbeddingException(String failureCode, String message) {
        super(message);
        this.failureCode = failureCode;
    }

    public MemberProfileEmbeddingException(String failureCode, String message, Throwable cause) {
        super(message, cause);
        this.failureCode = failureCode;
    }

    public String failureCode() {
        return failureCode;
    }
}

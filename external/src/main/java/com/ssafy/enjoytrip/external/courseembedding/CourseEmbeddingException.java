package com.ssafy.enjoytrip.external.courseembedding;

public class CourseEmbeddingException extends RuntimeException {
    private final String failureCode;

    public CourseEmbeddingException(String failureCode, String message) {
        super(message);
        this.failureCode = failureCode;
    }

    public CourseEmbeddingException(String failureCode, String message, Throwable cause) {
        super(message, cause);
        this.failureCode = failureCode;
    }

    public String failureCode() {
        return failureCode;
    }
}

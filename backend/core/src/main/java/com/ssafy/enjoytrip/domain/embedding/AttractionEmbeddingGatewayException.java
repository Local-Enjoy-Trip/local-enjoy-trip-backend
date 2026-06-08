package com.ssafy.enjoytrip.domain.embedding;

public class AttractionEmbeddingGatewayException extends RuntimeException {
    private final String failureCode;

    public AttractionEmbeddingGatewayException(String failureCode, String message) {
        super(message);
        this.failureCode = failureCode;
    }

    public AttractionEmbeddingGatewayException(String failureCode, String message, Throwable cause) {
        super(message, cause);
        this.failureCode = failureCode;
    }

    public String failureCode() {
        return failureCode;
    }
}

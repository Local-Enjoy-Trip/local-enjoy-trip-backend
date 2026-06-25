package com.ssafy.enjoytrip.external.aicourse;

public class AiCourseGenerationException extends RuntimeException {
    private final Reason reason;

    public AiCourseGenerationException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public AiCourseGenerationException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }

    public enum Reason {
        PROVIDER_ERROR,
        BLANK_RESPONSE,
        MALFORMED_RESPONSE
    }
}

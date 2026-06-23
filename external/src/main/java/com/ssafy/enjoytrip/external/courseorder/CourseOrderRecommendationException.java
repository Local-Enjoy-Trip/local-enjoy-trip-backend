package com.ssafy.enjoytrip.external.courseorder;

public class CourseOrderRecommendationException extends RuntimeException {
    private final Reason reason;

    public CourseOrderRecommendationException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public CourseOrderRecommendationException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }

    public enum Reason {
        TIMEOUT,
        BLANK_RESPONSE,
        MALFORMED_RESPONSE,
        PROVIDER_ERROR
    }
}

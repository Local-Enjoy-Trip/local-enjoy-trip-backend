package com.ssafy.enjoytrip.exception;

public class ExternalServiceException extends RuntimeException {
    private final Source source;

    public ExternalServiceException(Source source, Throwable cause) {
        super(source.message(), cause);
        this.source = source;
    }

    public Source source() {
        return source;
    }

    public enum Source {
        TOUR_API("Tour API call failed"),
        EV_CHARGER_API("EV charger API call failed");

        private final String message;

        Source(String message) {
            this.message = message;
        }

        public String message() {
            return message;
        }
    }
}

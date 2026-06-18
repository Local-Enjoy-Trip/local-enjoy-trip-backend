package com.ssafy.enjoytrip.core.support.error.exception;

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
        TOUR_API("Tour API 호출에 실패했습니다."),
        EV_CHARGER_API("EV 충전기 API 호출에 실패했습니다.");

        private final String message;

        Source(String message) {
            this.message = message;
        }

        public String message() {
            return message;
        }
    }
}

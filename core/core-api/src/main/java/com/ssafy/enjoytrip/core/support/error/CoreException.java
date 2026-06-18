package com.ssafy.enjoytrip.core.support.error;

public class CoreException extends RuntimeException {
    private final ErrorType errorType;

    public CoreException(ErrorType errorType) {
        super(errorType.message());
        this.errorType = errorType;
    }

    public CoreException(ErrorType errorType, Throwable cause) {
        super(errorType.message(), cause);
        this.errorType = errorType;
    }

    public ErrorType errorType() {
        return errorType;
    }
}

package com.ssafy.enjoytrip.support.response;

import com.ssafy.enjoytrip.support.error.ErrorCode;
import com.ssafy.enjoytrip.support.error.ErrorType;

public final class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final ErrorResponse error;

    private ApiResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> fail(ErrorType error) {
        return new ApiResponse<>(false, null, new ErrorResponse(error.code(), error.message()));
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public ErrorResponse getError() {
        return error;
    }
}

package com.ssafy.enjoytrip.support.response;

import com.ssafy.enjoytrip.support.error.ErrorCode;

public record ErrorResponse(
        ErrorCode code,
        String message
) {
}

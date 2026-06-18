package com.ssafy.enjoytrip.core.support.response;

import com.ssafy.enjoytrip.core.support.error.ErrorCode;

public record ErrorResponse(
        ErrorCode code,
        String message
) {
}

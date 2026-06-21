package com.ssafy.enjoytrip.core.support.response;

public record ErrorResponse(
        String code,
        String message
) {
}

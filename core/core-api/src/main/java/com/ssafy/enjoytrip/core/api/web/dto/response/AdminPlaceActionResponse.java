package com.ssafy.enjoytrip.core.api.web.dto.response;

public record AdminPlaceActionResponse(
        Long id,
        String status,
        String message,
        boolean visible,
        int totalCount,
        int pageSize,
        int currentPage,
        int totalPages,
        String nextActionUrl,
        String nextActionLabel,
        String nextActionStyle
) {
}

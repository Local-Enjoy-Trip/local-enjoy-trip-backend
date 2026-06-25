package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.support.error.exception.ClientInputException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record WrittenNotesRequest(
        @Schema(description = "작성 쪽지 조회 개수, 기본값 50", example = "50")
        @Positive @Max(100) Integer limit
) {
    private static final int DEFAULT_LIMIT = 50;
    private static final String INVALID_REQUEST_MESSAGE = "유효하지 않은 요청입니다.";

    public int normalizedLimit() {
        if (limit != null && (limit <= 0 || limit > 100)) {
            throw new ClientInputException(INVALID_REQUEST_MESSAGE);
        }

        return limit == null ? DEFAULT_LIMIT : limit;
    }
}

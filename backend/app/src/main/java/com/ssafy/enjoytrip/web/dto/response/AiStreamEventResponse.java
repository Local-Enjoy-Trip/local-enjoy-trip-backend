package com.ssafy.enjoytrip.web.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

public record AiStreamEventResponse(
        String id,
        String action,
        String clientId,
        String requestId,
        JsonNode payload
) {
}

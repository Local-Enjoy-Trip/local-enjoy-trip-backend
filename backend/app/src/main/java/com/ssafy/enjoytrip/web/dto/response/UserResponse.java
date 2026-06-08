package com.ssafy.enjoytrip.web.dto.response;

public record UserResponse(
        String userId,
        String name,
        String email,
        String createdAt
) {
}

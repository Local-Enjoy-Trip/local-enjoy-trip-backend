package com.ssafy.enjoytrip.web.api;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiEventApi {

    @Operation(summary = "AI 응답 SSE 구독", description = "인증된 사용자 ID로 Redis Stream AI 응답을 Server-Sent Events로 전달합니다.")
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter subscribe(@AuthenticationPrincipal Jwt jwt);
}

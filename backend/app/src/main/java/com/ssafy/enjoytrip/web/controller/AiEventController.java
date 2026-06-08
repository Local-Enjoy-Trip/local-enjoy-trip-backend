package com.ssafy.enjoytrip.web.controller;

import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.error.ErrorType;
import com.ssafy.enjoytrip.web.api.AiEventApi;
import com.ssafy.enjoytrip.web.sse.AiSseEventBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiEventController implements AiEventApi {

    private final AiSseEventBroadcaster broadcaster;

    @Override
    public SseEmitter subscribe(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new CoreException(ErrorType.AUTHENTICATION_REQUIRED);
        }
        return broadcaster.subscribe(jwt.getSubject());
    }
}

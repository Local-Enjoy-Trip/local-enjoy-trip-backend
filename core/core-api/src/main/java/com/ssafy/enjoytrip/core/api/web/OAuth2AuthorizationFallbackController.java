package com.ssafy.enjoytrip.core.api.web;

import com.ssafy.enjoytrip.core.support.error.exception.InfraUnavailableException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuth2AuthorizationFallbackController {
    private static final String GOOGLE_OAUTH_NOT_CONFIGURED_MESSAGE = "Google OAuth가 설정되어 있지 않습니다.";

    @GetMapping("/oauth2/authorization/google")
    void googleOAuthNotConfigured() {
        throw new InfraUnavailableException(GOOGLE_OAUTH_NOT_CONFIGURED_MESSAGE);
    }
}

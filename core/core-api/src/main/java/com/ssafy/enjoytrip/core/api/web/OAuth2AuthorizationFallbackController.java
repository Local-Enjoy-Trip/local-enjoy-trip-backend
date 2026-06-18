package com.ssafy.enjoytrip.core.api.web;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.GOOGLE_OAUTH_NOT_CONFIGURED;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuth2AuthorizationFallbackController {
    @GetMapping("/oauth2/authorization/google")
    void googleOAuthNotConfigured() {
        throw new CoreException(GOOGLE_OAUTH_NOT_CONFIGURED);
    }
}

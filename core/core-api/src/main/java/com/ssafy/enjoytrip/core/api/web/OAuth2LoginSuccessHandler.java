package com.ssafy.enjoytrip.core.api.web;

import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.domain.service.JwtTokenService;
import com.ssafy.enjoytrip.core.domain.service.MemberService;
import com.ssafy.enjoytrip.core.domain.service.OAuthSignupTicketService;
import com.ssafy.enjoytrip.core.api.web.dto.response.IssuedToken;
import com.ssafy.enjoytrip.core.api.web.dto.response.OAuthSignupTicket;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final MemberService memberService;
    private final JwtTokenService tokenService;
    private final OAuthSignupTicketService ticketService;

    @Value("${enjoytrip.oauth2.authorized-redirect-uri:http://localhost:5173/oauth/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
        OAuth2User principal = oauth.getPrincipal();
        String email = value(principal.getAttribute("email"));
        String name = value(principal.getAttribute("name"));
        String providerUserId = value(principal.getAttribute("sub"));
        if (email.isBlank()) {
            response.sendRedirect(redirectUri + "?error=" + encode("google_email_missing"));
            return;
        }
        if (providerUserId.isBlank()) {
            providerUserId = principal.getName();
        }

        String provider = oauth.getAuthorizedClientRegistrationId();
        Member member = memberService.findByEmail(email);
        if (member == null) {
            OAuthSignupTicket ticket = ticketService.issue(provider, providerUserId, email, name);
            response.sendRedirect(redirectUri + "#" + signupFragment(ticket));
            return;
        }

        memberService.loginWithOAuth(provider, providerUserId, email, name);
        IssuedToken token = tokenService.issue(member);
        response.sendRedirect(redirectUri + "#" + tokenFragment(token));
    }

    private static String tokenFragment(IssuedToken token) {
        return "accessToken=" + encode(token.accessToken())
                + "&tokenType=" + encode(token.tokenType())
                + "&expiresIn=" + token.expiresIn();
    }

    private static String signupFragment(OAuthSignupTicket ticket) {
        return "oauthSignupTicket=" + encode(ticket.ticket())
                + "&email=" + encode(ticket.email())
                + "&suggestedName=" + encode(ticket.suggestedName())
                + "&expiresIn=" + ticket.expiresIn();
    }

    private static String value(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value).trim();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

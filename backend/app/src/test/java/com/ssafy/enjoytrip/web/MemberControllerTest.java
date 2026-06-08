package com.ssafy.enjoytrip.web;
import com.ssafy.enjoytrip.web.api.*;
import com.ssafy.enjoytrip.web.controller.*;
import com.ssafy.enjoytrip.web.dto.request.*;
import com.ssafy.enjoytrip.web.dto.response.*;

import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.service.JwtTokenService;
import com.ssafy.enjoytrip.service.MemberService;
import com.ssafy.enjoytrip.service.OAuthSignupTicketService;
import com.ssafy.enjoytrip.service.OAuthSignupTicketService.PendingOAuthSignup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberControllerTest {
    private MemberService memberService;
    private JwtTokenService tokenService;
    private OAuthSignupTicketService oauthSignupTicketService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        memberService = mock(MemberService.class);
        tokenService = mock(JwtTokenService.class);
        oauthSignupTicketService = mock(OAuthSignupTicketService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new MemberController(memberService, tokenService, oauthSignupTicketService))
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void signupRequiresAllFields() throws Exception {
        mockMvc.perform(post("/api/members/signup")
                        .param("userId", "ssafy")
                        .param("password", "secret123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signupReturnsConflictWhenUserExists() throws Exception {
        when(memberService.existsByUserId("ssafy")).thenReturn(true);

        mockMvc.perform(post("/api/members/signup")
                        .param("userId", "ssafy")
                        .param("name", "SSAFY")
                        .param("email", "ssafy@example.com")
                        .param("password", "secret123"))
                .andExpect(status().isConflict());
    }

    @Test
    void signupValidatesFieldsAndEmailDuplicates() throws Exception {
        mockMvc.perform(post("/api/members/signup")
                        .param("userId", "bad id")
                        .param("name", "SSAFY")
                        .param("email", "ssafy@example.com")
                        .param("password", "secret123"))
                .andExpect(status().isBadRequest());

        when(memberService.existsByEmail("ssafy@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/members/signup")
                        .param("userId", "ssafy")
                        .param("name", "SSAFY")
                        .param("email", "ssafy@example.com")
                        .param("password", "secret123"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.message").value("Email already exists"));
    }

    @Test
    void loginReturnsJwtToken() throws Exception {
        Member member = new Member("ssafy", "SSAFY", "ssafy@example.com", "hidden", "2026-05-14 11:00:00");
        when(memberService.login("ssafy", "secret")).thenReturn(member);
        when(tokenService.issue(member)).thenReturn(new IssuedToken("jwt-token", "Bearer", 7200));

        mockMvc.perform(post("/api/members/login")
                        .param("userId", "ssafy")
                        .param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(7200))
                .andExpect(jsonPath("$.data.user.userId").value("ssafy"))
                .andExpect(jsonPath("$.data.user.password").doesNotExist());
    }

    @Test
    void oauthSignupCreatesMemberFromTicketAndReturnsJwtToken() throws Exception {
        Member member = new Member("google_123", "트래블러", "google@example.com", "hidden", "2026-05-14 11:00:00");
        when(oauthSignupTicketService.verify("ticket"))
                .thenReturn(new PendingOAuthSignup("google", "123", "google@example.com", "Google Name"));
        when(memberService.signupWithOAuth("google", "123", "google@example.com", "트래블러")).thenReturn(member);
        when(tokenService.issue(member)).thenReturn(new IssuedToken("jwt-token", "Bearer", 7200));

        mockMvc.perform(post("/api/members/oauth/signup")
                        .param("oauthSignupTicket", "ticket")
                        .param("name", "트래블러"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.name").value("트래블러"));
    }

    @Test
    void meRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsAuthenticatedUser() throws Exception {
        Member member = new Member("ssafy", "SSAFY", "ssafy@example.com", "hidden", "2026-05-14 11:00:00");
        when(memberService.findByUserId("ssafy")).thenReturn(member);

        mockMvc.perform(get("/api/members/me").principal(jwtPrincipal("ssafy")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.userId").value("ssafy"))
                .andExpect(jsonPath("$.data.user.email").value("ssafy@example.com"));
    }

    @Test
    void updateRejectsDifferentUserToken() throws Exception {
        mockMvc.perform(put("/api/members/other")
                        .principal(jwtPrincipal("ssafy"))
                        .param("name", "Other"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteRejectsDifferentUserToken() throws Exception {
        mockMvc.perform(delete("/api/members/other")
                        .principal(jwtPrincipal("ssafy")))
                .andExpect(status().isForbidden());
    }

    private static JwtAuthenticationToken jwtPrincipal(String userId) {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(userId)
                .claim("name", "SSAFY")
                .claim("email", "ssafy@example.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(7200))
                .build();
        return new JwtAuthenticationToken(jwt);
    }
}

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

    @DisplayName("회원가입은 모든 필드를 요구한다")
    @Test
    void signupRequiresAllFields() throws Exception {
        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "ssafy",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("회원가입은 사용자가 이미 있으면 충돌로 응답한다")
    @Test
    void signupReturnsConflictWhenUserExists() throws Exception {
        when(memberService.existsByUserId("ssafy")).thenReturn(true);

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("ssafy", "SSAFY", "ssafy@example.com", "secret123")))
                .andExpect(status().isConflict());
    }

    @DisplayName("회원가입은 필드와 이메일 중복을 검증한다")
    @Test
    void signupValidatesFieldsAndEmailDuplicates() throws Exception {
        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("bad id", "SSAFY", "ssafy@example.com", "secret123")))
                .andExpect(status().isBadRequest());

        when(memberService.existsByEmail("ssafy@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("ssafy", "SSAFY", "ssafy@example.com", "secret123")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.message").value("Email already exists"));
    }

    @DisplayName("로그인은 JWT 토큰을 반환한다")
    @Test
    void loginReturnsJwtToken() throws Exception {
        Member member = new Member("ssafy", "SSAFY", "ssafy@example.com", "hidden", "2026-05-14 11:00:00");
        when(memberService.login("ssafy", "secret")).thenReturn(member);
        when(tokenService.issue(member)).thenReturn(new IssuedToken("jwt-token", "Bearer", 7200));

        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "ssafy",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(7200))
                .andExpect(jsonPath("$.data.user.userId").value("ssafy"))
                .andExpect(jsonPath("$.data.user.password").doesNotExist());
    }

    @DisplayName("OAuth 회원가입은 티켓으로 회원을 만들고 JWT 토큰을 반환한다")
    @Test
    void oauthSignupCreatesMemberFromTicketAndReturnsJwtToken() throws Exception {
        Member member = new Member("google_123", "트래블러", "google@example.com", "hidden", "2026-05-14 11:00:00");
        when(oauthSignupTicketService.verify("ticket"))
                .thenReturn(new PendingOAuthSignup("google", "123", "google@example.com", "Google Name"));
        when(memberService.signupWithOAuth("google", "123", "google@example.com", "트래블러")).thenReturn(member);
        when(tokenService.issue(member)).thenReturn(new IssuedToken("jwt-token", "Bearer", 7200));

        mockMvc.perform(post("/api/members/oauth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oauthSignupTicket": "ticket",
                                  "name": "트래블러"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.name").value("트래블러"));
    }

    @DisplayName("내 정보 조회는 인증을 요구한다")
    @Test
    void meRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("내 정보 조회는 인증된 사용자를 반환한다")
    @Test
    void meReturnsAuthenticatedUser() throws Exception {
        Member member = new Member(
                "ssafy",
                "SSAFY",
                "동네핀러",
                "ssafy@example.com",
                "hidden",
                "https://cdn.example.com/profile.png",
                37.5665,
                126.9780,
                "서울 중구",
                "2026-05-14 11:00:00"
        );
        when(memberService.findByUserId("ssafy")).thenReturn(member);

        mockMvc.perform(get("/api/members/me").principal(jwtPrincipal("ssafy")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.userId").value("ssafy"))
                .andExpect(jsonPath("$.data.user.email").value("ssafy@example.com"))
                .andExpect(jsonPath("$.data.user.nickname").value("동네핀러"))
                .andExpect(jsonPath("$.data.user.profileImageUrl").value("https://cdn.example.com/profile.png"))
                .andExpect(jsonPath("$.data.user.representativeLatitude").value(37.5665))
                .andExpect(jsonPath("$.data.user.representativeLongitude").value(126.9780))
                .andExpect(jsonPath("$.data.user.representativeRegionName").value("서울 중구"));
    }

    @DisplayName("내 정보 수정은 닉네임과 프로필 이미지와 대표 위치를 변경한다")
    @Test
    void updateMeChangesProfileAndRepresentativeLocation() throws Exception {
        when(memberService.update(any(Member.class))).thenReturn(true);

        mockMvc.perform(put("/api/members/me")
                        .principal(jwtPrincipal("ssafy"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "동네핀러",
                                  "profileImageUrl": "https://cdn.example.com/profile.png",
                                  "representativeLatitude": 37.5665,
                                  "representativeLongitude": 126.9780,
                                  "representativeRegionName": "서울 중구"
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberService).update(memberCaptor.capture());
        Member member = memberCaptor.getValue();
        assertThat(member.userId()).isEqualTo("ssafy");
        assertThat(member.nickname()).isEqualTo("동네핀러");
        assertThat(member.profileImageUrl()).isEqualTo("https://cdn.example.com/profile.png");
        assertThat(member.representativeLatitude()).isEqualTo(37.5665);
        assertThat(member.representativeLongitude()).isEqualTo(126.9780);
        assertThat(member.representativeRegionName()).isEqualTo("서울 중구");
    }

    @DisplayName("내 정보 수정은 대표 위치 위도와 경도를 함께 요구한다")
    @Test
    void updateMeRequiresBothLatitudeAndLongitude() throws Exception {
        mockMvc.perform(put("/api/members/me")
                        .principal(jwtPrincipal("ssafy"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "representativeLatitude": 37.5665
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Invalid latitude or longitude"));
    }

    @DisplayName("내 정보 수정은 다른 사용자 토큰을 거부한다")
    @Test
    void updateRejectsDifferentUserToken() throws Exception {
        mockMvc.perform(put("/api/members/other")
                        .principal(jwtPrincipal("ssafy"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Other"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @DisplayName("회원 삭제는 다른 사용자 토큰을 거부한다")
    @Test
    void deleteRejectsDifferentUserToken() throws Exception {
        mockMvc.perform(delete("/api/members/other")
                        .principal(jwtPrincipal("ssafy")))
                .andExpect(status().isForbidden());
    }

    private static String signupJson(String userId, String name, String email, String password) {
        return """
                {
                  "userId": "%s",
                  "name": "%s",
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(userId, name, email, password);
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

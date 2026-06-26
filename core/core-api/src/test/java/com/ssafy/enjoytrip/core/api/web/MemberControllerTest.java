package com.ssafy.enjoytrip.core.api.web;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ssafy.enjoytrip.core.api.web.controller.MemberController;
import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.domain.service.MemberService;
import com.ssafy.enjoytrip.core.support.auth.IssuedToken;
import com.ssafy.enjoytrip.core.support.auth.JwtTokenService;
import com.ssafy.enjoytrip.core.support.auth.OAuthSignupTicketService;
import com.ssafy.enjoytrip.core.support.auth.OAuthSignupTicketService.PendingOAuthSignup;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
        mockMvc = MockMvcBuilders.standaloneSetup(new MemberController(
                        memberService,
                        tokenService,
                        oauthSignupTicketService
                ))
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @DisplayName("회원가입은 이메일, 이름, 비밀번호를 요구한다")
    @Test
    void signupRequiresAllFields() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("회원가입은 사용자가 이미 있으면 충돌로 응답한다")
    @Test
    void signupReturnsConflictWhenUserExists() throws Exception {
        doThrow(new CoreException(USER_ALREADY_EXISTS)).when(memberService).signup(any(Member.class));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("SSAFY", "Case@Test.com", "secret123")))
                .andExpect(status().isConflict());
    }

    @DisplayName("회원가입은 legacy userId 필드를 거부한다")
    @Test
    void signupRejectsLegacyUserIdField() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "ssafy",
                                  "name": "SSAFY",
                                  "email": "Case@Test.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("로그인은 JWT 토큰과 userId를 반환하고 password를 응답하지 않는다")
    @Test
    void loginReturnsJwtToken() throws Exception {
        Member member = new Member(1L, "SSAFY", "동네핀러", "Case@Test.com", "hidden", null);
        when(memberService.login("Case@Test.com", "secret")).thenReturn(member);
        when(tokenService.issue(member)).thenReturn(new IssuedToken("jwt-token", "Bearer", 7200));

        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "Case@Test.com",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(7200))
                .andExpect(jsonPath("$.data.user.userId").value("1"))
                .andExpect(jsonPath("$.data.user.email").value("Case@Test.com"))
                .andExpect(jsonPath("$.data.user.password").doesNotExist());
    }

    @DisplayName("로그인은 legacy userId 필드를 거부한다")
    @Test
    void loginRejectsLegacyUserIdField() throws Exception {
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "ssafy",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("OAuth 회원가입은 티켓으로 회원을 만들고 JWT 토큰을 반환한다")
    @Test
    void oauthSignupCreatesMemberFromTicketAndReturnsJwtToken() throws Exception {
        Member member = new Member(2L, "김구글", "트래블러", "google@example.com", "hidden", null);
        when(oauthSignupTicketService.verify("ticket"))
                .thenReturn(new PendingOAuthSignup("google", "123", "google@example.com", "Google Name"));
        when(memberService.signupWithOAuth(
                "google",
                "123",
                "google@example.com",
                "김구글",
                "트래블러"
        ))
                .thenReturn(member);
        when(tokenService.issue(member)).thenReturn(new IssuedToken("jwt-token", "Bearer", 7200));

        mockMvc.perform(post("/api/members/oauth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oauthSignupTicket": "ticket",
                                  "name": "김구글",
                                  "nickname": "트래블러"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.name").value("김구글"))
                .andExpect(jsonPath("$.data.user.nickname").value("트래블러"));
    }

    @DisplayName("잘못된 OAuth 가입 티켓은 클라이언트 입력 오류로 응답한다")
    @Test
    void oauthSignupRejectsInvalidTicketAsClientInput() throws Exception {
        when(oauthSignupTicketService.verify("broken-ticket"))
                .thenThrow(new JwtException("invalid token"));

        mockMvc.perform(post("/api/members/oauth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oauthSignupTicket": "broken-ticket",
                                  "name": "김구글",
                                  "nickname": "트래블러"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("C400"))
                .andExpect(jsonPath("$.error.message").value("유효하지 않은 요청입니다."));
    }

    @DisplayName("OAuth 회원가입은 닉네임을 필수로 요구한다")
    @Test
    void oauthSignupRequiresNickname() throws Exception {
        mockMvc.perform(post("/api/members/oauth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oauthSignupTicket": "ticket",
                                  "name": "김구글"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("C400"));
    }

    @DisplayName("OAuth 회원가입은 이미 존재하는 사용자 정보이면 충돌로 응답한다")
    @Test
    void oauthSignupRejectsExistingMember() throws Exception {
        when(oauthSignupTicketService.verify("ticket"))
                .thenReturn(new PendingOAuthSignup("google", "123", "google@example.com", "Google Name"));
        doThrow(new CoreException(USER_ALREADY_EXISTS))
                .when(memberService)
                .signupWithOAuth(
                        "google",
                        "123",
                        "google@example.com",
                        "김구글",
                        "트래블러"
                );

        mockMvc.perform(post("/api/members/oauth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oauthSignupTicket": "ticket",
                                  "name": "김구글",
                                  "nickname": "트래블러"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.message").value("이미 존재하는 사용자입니다."));
    }

    @DisplayName("내 정보 조회는 인증된 사용자를 반환한다")
    @Test
    void meReturnsAuthenticatedUser() throws Exception {
        Member member = new Member(
                1L,
                "SSAFY",
                "동네핀러",
                "ssafy@example.com",
                "hidden",
                "https://cdn.example.com/profile.png"
        );
        when(memberService.findRequiredById(1L)).thenReturn(member);

        mockMvc.perform(get("/api/members/me").principal(jwtPrincipal("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.userId").value("1"))
                .andExpect(jsonPath("$.data.user.email").value("ssafy@example.com"))
                .andExpect(jsonPath("$.data.user.nickname").value("동네핀러"))
                .andExpect(jsonPath("$.data.user.profileImageUrl")
                        .value("https://cdn.example.com/profile.png"));
    }

    @DisplayName("내 정보 수정은 닉네임만 변경하고 프로필 이미지는 변경하지 않는다")
    @Test
    void updateMeChangesProfile() throws Exception {
        mockMvc.perform(put("/api/members/me")
                        .principal(jwtPrincipal("1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "동네핀러"
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberService).update(memberCaptor.capture());
        Member member = memberCaptor.getValue();
        assertThat(member.memberId()).isEqualTo(1L);
        assertThat(member.name()).isNull();
        assertThat(member.nickname()).isEqualTo("동네핀러");
        assertThat(member.email()).isNull();
        assertThat(member.password()).isNull();
        assertThat(member.profileImageUrl()).isNull();
    }

    private static String signupJson(String name, String email, String password) {
        return """
                {
                  "name": "%s",
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(name, email, password);
    }

    private static JwtAuthenticationToken jwtPrincipal(String subject) {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(subject)
                .claim("name", "SSAFY")
                .claim("email", "ssafy@example.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(7200))
                .build();
        return new JwtAuthenticationToken(jwt);
    }
}

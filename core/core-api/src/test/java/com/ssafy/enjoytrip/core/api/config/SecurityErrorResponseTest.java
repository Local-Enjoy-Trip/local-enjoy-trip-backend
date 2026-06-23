package com.ssafy.enjoytrip.core.api.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.enjoytrip.core.api.security.AuthenticatedUserIdArgumentResolver;
import com.ssafy.enjoytrip.core.api.web.controller.MemberController;
import com.ssafy.enjoytrip.core.support.auth.JwtTokenService;
import com.ssafy.enjoytrip.core.domain.service.MemberService;
import com.ssafy.enjoytrip.core.support.auth.OAuthSignupTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringJUnitWebConfig(SecurityErrorResponseTest.TestConfig.class)
class SecurityErrorResponseTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @DisplayName("보호 API 인증 실패는 Security entrypoint가 공통 오류 응답으로 처리한다")
    @Test
    void authenticationFailureUsesSecurityEntryPoint() throws Exception {
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("S401"))
                .andExpect(jsonPath("$.error.message").value("인증이 필요합니다."));
    }

    @DisplayName("POST 보호 API 인증 실패도 Security entrypoint가 공통 오류 응답으로 처리한다")
    @Test
    void postAuthenticationFailureUsesSecurityEntryPoint() throws Exception {
        mockMvc.perform(post("/api/friendships/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetUserId\":\"bob\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("S401"))
                .andExpect(jsonPath("$.error.message").value("인증이 필요합니다."));

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"제목",
                                  "content":"내용",
                                  "category":"TIP",
                                  "visibility":"PUBLIC",
                                  "latitude":37.5665,
                                  "longitude":126.9780
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("S401"))
                .andExpect(jsonPath("$.error.message").value("인증이 필요합니다."));

        mockMvc.perform(post("/api/note-images/presigned-upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentType\":\"image/jpeg\",\"fileExtension\":\"jpg\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("S401"))
                .andExpect(jsonPath("$.error.message").value("인증이 필요합니다."));

        mockMvc.perform(post("/api/members/me/profile-image/presigned-upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentType\":\"image/jpeg\",\"fileExtension\":\"jpg\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("S401"))
                .andExpect(jsonPath("$.error.message").value("인증이 필요합니다."));

        mockMvc.perform(put("/api/members/me/profile-image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "objectKey":"profiles/ssafy/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg",
                                  "contentType":"image/jpeg"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("S401"))
                .andExpect(jsonPath("$.error.message").value("인증이 필요합니다."));

        mockMvc.perform(post("/api/courses/course-1/order-recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentLatitude": 37.5665,
                                  "currentLongitude": 126.9780
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("S401"))
                .andExpect(jsonPath("$.error.message").value("인증이 필요합니다."));
    }

    @DisplayName("지도 탐색 인증 실패도 Security entrypoint가 공통 오류 응답으로 처리한다")
    @Test
    void mapExploreAuthenticationFailureUsesSecurityEntryPoint() throws Exception {
        mockMvc.perform(get("/api/map/explore"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("S401"))
                .andExpect(jsonPath("$.error.message").value("인증이 필요합니다."));
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import(SecurityConfig.class)
    static class TestConfig {
        @Bean
        WebConfig webConfig(AuthenticatedUserIdArgumentResolver resolver) {
            return new WebConfig(resolver);
        }

        @Bean
        AuthenticatedUserIdArgumentResolver authenticatedUserIdArgumentResolver() {
            return new AuthenticatedUserIdArgumentResolver();
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        MemberController memberController(MemberService memberService,
                                          JwtTokenService tokenService,
                                          OAuthSignupTicketService oauthSignupTicketService) {
            return new MemberController(memberService, tokenService, oauthSignupTicketService);
        }

        @Bean
        MemberService memberService() {
            return Mockito.mock(MemberService.class);
        }

        @Bean
        JwtTokenService tokenService() {
            return Mockito.mock(JwtTokenService.class);
        }

        @Bean
        OAuthSignupTicketService oauthSignupTicketService() {
            return Mockito.mock(OAuthSignupTicketService.class);
        }
    }
}

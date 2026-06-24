package com.ssafy.enjoytrip.core.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.enjoytrip.core.api.security.AdminAccountDetailsService;
import com.ssafy.enjoytrip.core.api.security.AdminAuthenticationSupport;
import com.ssafy.enjoytrip.core.api.security.DbBackedJwtAuthenticationConverter;
import com.ssafy.enjoytrip.core.domain.MemberRole;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringJUnitWebConfig(AdminSecurityAuthorizationTest.TestConfig.class)
class AdminSecurityAuthorizationTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        Mockito.reset(memberMapper);
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @DisplayName("JWT 권한 변환기는 매 요청마다 DB role을 ROLE_ADMIN으로 반영한다")
    @Test
    void jwtConverterReadsMemberRoleFromDatabase() {
        MemberMapper memberMapper = Mockito.mock(MemberMapper.class);
        MemberRecord admin = member(1L, "관리자", "관리자", "admin@example.com", "encoded", MemberRole.ADMIN.name());
        Mockito.when(memberMapper.findById(1L)).thenReturn(admin);

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("1")
                .build();
        JwtAuthenticationToken authentication = (JwtAuthenticationToken)
                new DbBackedJwtAuthenticationConverter(memberMapper).convert(jwt);

        assertThat(authentication.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_ADMIN");
    }

    @DisplayName("JWT 권한 변환기는 숫자가 아닌 subject를 인증 실패로 처리한다")
    @Test
    void jwtConverterRejectsNonNumericSubject() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("legacy-user")
                .build();

        assertThatThrownBy(() -> new DbBackedJwtAuthenticationConverter(memberMapper).convert(jwt))
                .isInstanceOf(BadCredentialsException.class);
    }

    @DisplayName("관리자 이메일 로그인 계정은 내부 회원 role이 ADMIN일 때만 로드된다")
    @Test
    void adminAccountDetailsServiceLoadsOnlyAdminEmailAccount() {
        MemberMapper memberMapper = Mockito.mock(MemberMapper.class);
        MemberRecord admin = member(1L, "관리자", "관리자", "admin@example.com", "encoded", MemberRole.ADMIN.name());
        Mockito.when(memberMapper.findByEmail("admin@example.com")).thenReturn(admin);

        UserDetails details = new AdminAccountDetailsService(memberMapper)
                .loadUserByUsername("admin@example.com");

        assertThat(details.getUsername()).isEqualTo("1");
        assertThat(details.getPassword()).isEqualTo("encoded");
        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_ADMIN");
    }

    @DisplayName("관리자 HTML 경로는 DB role USER를 403으로 차단하고 ADMIN만 통과시킨다")
    @Test
    void adminHtmlPathRequiresAdminRole() throws Exception {
        MemberRecord user = member(2L, "사용자", "사용자", "user@example.com", "encoded", "USER");
        MemberRecord admin = member(1L, "관리자", "관리자", "admin@example.com", "encoded", MemberRole.ADMIN.name());
        Mockito.when(memberMapper.findById(2L)).thenReturn(user);
        Mockito.when(memberMapper.findById(1L)).thenReturn(admin);

        mockMvc.perform(get("/admin/test"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/admin/test").with(user("2")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/test").with(user("1")))
                .andExpect(status().isOk());
    }

    @DisplayName("관리자 HTML POST는 ADMIN 권한과 CSRF 토큰이 모두 있어야 통과한다")
    @Test
    void adminHtmlPostRequiresCsrfToken() throws Exception {
        MemberRecord admin = member(
                1L,
                "관리자",
                "관리자",
                "admin@example.com",
                passwordEncoder.encode("secret"),
                MemberRole.ADMIN.name()
        );
        Mockito.when(memberMapper.findByEmail("admin@example.com")).thenReturn(admin);
        Mockito.when(memberMapper.findById(1L)).thenReturn(admin);

        MvcResult login = mockMvc.perform(formLogin("/admin/login")
                        .user("email", "admin@example.com")
                        .password("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(post("/admin/test").session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/test")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    private static MemberRecord member(Long id,
                                       String name,
                                       String nickname,
                                       String email,
                                       String password,
                                       String role) {
        MemberRecord member = new MemberRecord(name, nickname, email, password, null);
        member.setId(id);
        member.setRole(role);
        return member;
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import(SecurityConfig.class)
    static class TestConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        MemberMapper memberMapper() {
            return Mockito.mock(MemberMapper.class);
        }

        @Bean
        AdminAuthenticationSupport adminAuthenticationSupport(MemberMapper memberMapper) {
            return new AdminAuthenticationSupport(memberMapper);
        }

        @Bean
        AdminOnlyTestController adminOnlyTestController() {
            return new AdminOnlyTestController();
        }
    }

    @RestController
    static class AdminOnlyTestController {
        @GetMapping("/admin/test")
        String admin() {
            return "ok";
        }

        @PostMapping("/admin/test")
        String mutateAdmin() {
            return "ok";
        }
    }
}

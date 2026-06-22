package com.ssafy.enjoytrip.core.api.config;

import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.domain.service.JwtTokenService;
import com.ssafy.enjoytrip.core.domain.service.IssuedToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import com.ssafy.enjoytrip.core.api.security.AuthenticatedUserIdArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("security")
class SecuritySupportTest {

    @DisplayName("JWT 설정이 비어 있거나 잘못되어도 안전한 기본값을 제공한다")
    @Test
    void jwtPropertiesProvideSafeDefaultsForBlankOrInvalidConfiguration() {
        JwtProperties defaults = new JwtProperties(" ", -1);

        assertThat(defaults.secret()).isNotBlank();
        assertThat(defaults.expirationSeconds()).isEqualTo(7200);
    }

    @DisplayName("JWT 명시 설정값을 그대로 보존한다")
    @Test
    void jwtPropertiesPreserveExplicitConfiguration() {
        JwtProperties explicit = new JwtProperties("01234567890123456789012345678901", 60);

        assertThat(explicit.secret()).isEqualTo("01234567890123456789012345678901");
        assertThat(explicit.expirationSeconds()).isEqualTo(60);
    }

    @DisplayName("비밀번호 인코더는 BCrypt를 사용한다")
    @Test
    void passwordEncoderUsesBcrypt() {
        PasswordEncoder passwordEncoder = new SecurityConfig().passwordEncoder();

        String encoded = passwordEncoder.encode("secret");

        assertThat(encoded).startsWith("$2");
        assertThat(passwordEncoder.matches("secret", encoded)).isTrue();
    }

    @DisplayName("CORS는 로컬 개발 출처와 JWT 헤더를 허용한다")
    @Test
    void corsAllowsLocalhostDevelopmentOriginsAndJwtHeaders() {
        TestCorsRegistry registry = new TestCorsRegistry();

        new WebConfig(new AuthenticatedUserIdArgumentResolver()).addCorsMappings(registry);

        CorsConfiguration configuration = registry.corsConfigurations().get("/**");
        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOriginPatterns()).contains("http://localhost:*", "http://127.0.0.1:*");
        assertThat(configuration.getAllowedMethods()).contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(configuration.getAllowedHeaders()).contains("Authorization", "Content-Type", "Accept");
        assertThat(configuration.getExposedHeaders()).contains("Authorization");
        assertThat(configuration.getAllowCredentials()).isTrue();
    }

    @DisplayName("발급한 JWT는 설정된 시크릿으로 복호화되고 회원 클레임을 포함한다")
    @Test
    void issuedJwtCanBeDecodedWithConfiguredSecretAndContainsMemberClaims() {
        JwtProperties properties = new JwtProperties("01234567890123456789012345678901", 120);
        SecurityConfig securityConfig = new SecurityConfig();
        JwtTokenService tokenService = new JwtTokenService(securityConfig.jwtEncoder(properties), properties);

        IssuedToken issued = tokenService.issue(
                new Member("ssafy", "SSAFY", "ssafy@example.com", "hidden")
        );
        Jwt decoded = securityConfig.jwtDecoder(properties).decode(issued.accessToken());

        assertThat(issued.tokenType()).isEqualTo("Bearer");
        assertThat(issued.expiresIn()).isEqualTo(120);
        assertThat(decoded.getClaimAsString("iss")).isEqualTo("enjoytrip");
        assertThat(decoded.getSubject()).isEqualTo("ssafy");
        assertThat(decoded.getClaimAsString("name")).isEqualTo("SSAFY");
        assertThat(decoded.getClaimAsString("email")).isEqualTo("ssafy@example.com");
    }

    private static class TestCorsRegistry extends CorsRegistry {
        Map<String, CorsConfiguration> corsConfigurations() {
            return getCorsConfigurations();
        }
    }
}

package com.ssafy.enjoytrip.config;

import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.service.JwtTokenService;
import com.ssafy.enjoytrip.web.dto.response.IssuedToken;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("security")
class SecuritySupportTest {

    @Test
    void jwtPropertiesProvideSafeDefaultsForBlankOrInvalidConfiguration() {
        JwtProperties defaults = new JwtProperties(" ", -1);

        assertThat(defaults.secret()).isNotBlank();
        assertThat(defaults.expirationSeconds()).isEqualTo(7200);
    }

    @Test
    void jwtPropertiesPreserveExplicitConfiguration() {
        JwtProperties explicit = new JwtProperties("01234567890123456789012345678901", 60);

        assertThat(explicit.secret()).isEqualTo("01234567890123456789012345678901");
        assertThat(explicit.expirationSeconds()).isEqualTo(60);
    }

    @Test
    void passwordEncoderUsesBcrypt() {
        PasswordEncoder passwordEncoder = new SecurityConfig().passwordEncoder();

        String encoded = passwordEncoder.encode("secret");

        assertThat(encoded).startsWith("$2");
        assertThat(passwordEncoder.matches("secret", encoded)).isTrue();
    }

    @Test
    void corsAllowsLocalhostDevelopmentOriginsAndJwtHeaders() {
        TestCorsRegistry registry = new TestCorsRegistry();

        new WebConfig().addCorsMappings(registry);

        CorsConfiguration configuration = registry.corsConfigurations().get("/**");
        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOriginPatterns()).contains("http://localhost:*", "http://127.0.0.1:*");
        assertThat(configuration.getAllowedMethods()).contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(configuration.getAllowedHeaders()).contains("Authorization", "Content-Type", "Accept");
        assertThat(configuration.getExposedHeaders()).contains("Authorization");
        assertThat(configuration.getAllowCredentials()).isTrue();
    }

    @Test
    void issuedJwtCanBeDecodedWithConfiguredSecretAndContainsMemberClaims() {
        JwtProperties properties = new JwtProperties("01234567890123456789012345678901", 120);
        SecurityConfig securityConfig = new SecurityConfig();
        JwtTokenService tokenService = new JwtTokenService(securityConfig.jwtEncoder(properties), properties);

        IssuedToken issued = tokenService.issue(
                new Member("ssafy", "SSAFY", "ssafy@example.com", "hidden", "")
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

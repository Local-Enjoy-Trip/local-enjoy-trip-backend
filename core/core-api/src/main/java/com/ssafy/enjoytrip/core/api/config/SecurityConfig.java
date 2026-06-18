package com.ssafy.enjoytrip.core.api.config;

import java.io.IOException;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import jakarta.servlet.http.HttpServletResponse;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
    private static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                           ObjectProvider<ClientRegistrationRepository> clientRegistrations,
                                           ObjectProvider<AuthenticationSuccessHandler> successHandler,
                                           ObjectProvider<AuthenticationFailureHandler> failureHandler)
            throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/members/me").authenticated()
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/members/me",
                                "/api/members/{userId}"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/members/me",
                                "/api/members/{userId}"
                        ).authenticated()
                        .requestMatchers(HttpMethod.PUT,
                                "/api/attractions/{id}/favorite",
                                "/api/attractions/{id}/rating",
                                "/api/attractions/{id}/tags").authenticated()
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/attractions/{id}/favorite",
                                "/api/attractions/{id}/rating").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/attraction-tags").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/attraction-tags/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/attraction-tags/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/map/explore").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/notes").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/note-images/presigned-upload").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/notes/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/notes/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/map/explore").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/note-images/presigned-upload").authenticated()
                        .requestMatchers("/api/friendships/**", "/api/notifications/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/plans", "/api/plans/items").authenticated()
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/plans/{id}",
                                "/api/plans/{id}/items"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/plans/{id}",
                                "/api/plans/{id}/items/{itemId}"
                        ).authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, exception) ->
                                writeFailure(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        ErrorType.AUTHENTICATION_REQUIRED))
                        .accessDeniedHandler((request, response, exception) ->
                                writeFailure(response, HttpServletResponse.SC_FORBIDDEN, ErrorType.ACCESS_DENIED))
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        if (clientRegistrations.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .successHandler(successHandler.getIfAvailable())
                    .failureHandler(failureHandler.getIfAvailable()));
        }
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtEncoder jwtEncoder(JwtProperties properties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey(properties)));
    }

    @Bean
    JwtDecoder jwtDecoder(JwtProperties properties) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey(properties))
                .macAlgorithm(JWT_ALGORITHM)
                .build();
    }

    private static SecretKey jwtSecretKey(JwtProperties properties) {
        return new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private static void writeFailure(HttpServletResponse response, int status, ErrorType error) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"success":false,"data":null,"error":{"code":"%s","message":"%s"}}
                """.formatted(error.code(), error.message()));
    }
}

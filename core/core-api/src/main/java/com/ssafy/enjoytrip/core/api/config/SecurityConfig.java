package com.ssafy.enjoytrip.core.api.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.ssafy.enjoytrip.core.api.security.AdminAccountDetailsService;
import com.ssafy.enjoytrip.core.api.security.AdminAuthenticationSupport;
import com.ssafy.enjoytrip.core.api.security.DbBackedJwtAuthenticationConverter;
import com.ssafy.enjoytrip.core.support.error.ErrorCode;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.support.auth.JwtProperties;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
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

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
    private static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

    @Bean
    @Order(1)
    SecurityFilterChain adminSecurityFilterChain(HttpSecurity http,
                                                 ObjectProvider<MemberMapper> memberMapper,
                                                 ObjectProvider<AdminAuthenticationSupport> adminSupport,
                                                 PasswordEncoder passwordEncoder)
            throws Exception {
        MemberMapper roleMapper = memberMapper.getIfAvailable();
        AdminAuthenticationSupport support = adminSupport.getIfAvailable();
        http.authenticationProvider(adminAuthenticationProvider(roleMapper, passwordEncoder));
        http
                .securityMatcher("/admin/**")
                .cors(Customizer.withDefaults())
                .csrf(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/admin/login", "/admin/forbidden").permitAll()
                        .requestMatchers(HttpMethod.POST, "/admin/login").permitAll()
                        .anyRequest().access((authentication, context) -> new AuthorizationDecision(
                                support != null && support.isAdmin(authentication.get())
                        ))
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/admin", true)
                        .failureUrl("/admin/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout")
                        .permitAll())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendRedirect("/admin/login"))
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            request.getRequestDispatcher("/admin/forbidden").forward(request, response);
                        })
                );
        return http.build();
    }

    private static DaoAuthenticationProvider adminAuthenticationProvider(
            MemberMapper memberMapper,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(
                new AdminAccountDetailsService(memberMapper)
        );
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    @Order(2)
    SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            ObjectProvider<ClientRegistrationRepository> clientRegistrations,
            ObjectProvider<AuthenticationSuccessHandler> successHandler,
            ObjectProvider<AuthenticationFailureHandler> failureHandler,
            ObjectProvider<MemberMapper> memberMapper,
            ObjectMapper objectMapper
    )
            throws Exception {
        MemberMapper roleMapper = memberMapper.getIfAvailable();
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/members/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/members/me").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/members/me").authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/members/me/profile-image/presigned-upload"
                        ).authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/members/me/profile-image").authenticated()
                        .requestMatchers(HttpMethod.PUT,
                                "/api/attractions/{id}/save",
                                "/api/attractions/{id}/rating",
                                "/api/attractions/{id}/tags").authenticated()
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/attractions/{id}/save",
                                "/api/attractions/{id}/rating").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/attraction-tags").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/attraction-tags/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/attraction-tags/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/map/explore").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/map/search").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/notes").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/note-images/presigned-upload").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/notes/saved").authenticated()
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/notes/{id}",
                                "/api/notes/{id}/save"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/notes/{id}",
                                "/api/notes/{id}/save"
                        ).authenticated()
                        .requestMatchers("/api/friendships/**", "/api/notifications/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/courses/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/courses").authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/courses/{id}/order-recommendation"
                        ).authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/courses/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/{id}").authenticated()
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
                        .authenticationEntryPoint((request, response, exception) -> writeError(
                                objectMapper,
                                response,
                                HttpStatus.UNAUTHORIZED,
                                ErrorCode.S401,
                                "인증이 필요합니다."
                        ))
                        .accessDeniedHandler((request, response, exception) -> writeError(
                                objectMapper,
                                response,
                                HttpStatus.FORBIDDEN,
                                ErrorCode.S403,
                                "접근 권한이 없습니다."
                        ))
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .jwtAuthenticationConverter(new DbBackedJwtAuthenticationConverter(roleMapper))));

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

    private static void writeError(
            ObjectMapper objectMapper,
            HttpServletResponse response,
            HttpStatus status,
            ErrorCode code,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(code, message));
    }

}

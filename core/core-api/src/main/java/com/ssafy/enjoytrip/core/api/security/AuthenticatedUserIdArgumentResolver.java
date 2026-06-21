package com.ssafy.enjoytrip.core.api.security;

import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthenticatedUserIdArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String AUTHENTICATION_REQUIRED_MESSAGE = "인증이 필요합니다.";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticatedUserId.class)
                && String.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        AuthenticatedUserId annotation = parameter.getParameterAnnotation(AuthenticatedUserId.class);
        String userId = authenticatedUserId(SecurityContextHolder.getContext().getAuthentication());
        if (userId == null || userId.isBlank()) {
            return unauthenticatedValue(annotation.unauthenticated());
        }
        return userId.strip();
    }

    private static String authenticatedUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication.getToken().getSubject();
        }
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return null;
    }

    private static String unauthenticatedValue(AuthenticatedUserId.Unauthenticated policy) {
        return switch (policy) {
            case THROW -> throw new AuthenticationCredentialsNotFoundException(AUTHENTICATION_REQUIRED_MESSAGE);
            case NULL -> null;
        };
    }
}

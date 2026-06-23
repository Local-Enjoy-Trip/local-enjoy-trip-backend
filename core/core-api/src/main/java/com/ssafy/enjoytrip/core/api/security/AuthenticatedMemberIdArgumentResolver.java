package com.ssafy.enjoytrip.core.api.security;

import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthenticatedMemberIdArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String AUTHENTICATION_REQUIRED_MESSAGE = "인증이 필요합니다.";
    private static final String INVALID_SUBJECT_MESSAGE = "유효하지 않은 인증 주체입니다.";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticatedMemberId.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        AuthenticatedMemberId annotation = parameter.getParameterAnnotation(AuthenticatedMemberId.class);
        String subject = authenticatedSubject(SecurityContextHolder.getContext().getAuthentication());
        if (subject == null || subject.isBlank()) {
            return unauthenticatedValue(annotation.unauthenticated());
        }
        return parseMemberId(subject);
    }

    private static String authenticatedSubject(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication.getToken().getSubject();
        }
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return authentication.getName();
    }

    private static Long parseMemberId(String subject) {
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw new BadCredentialsException(INVALID_SUBJECT_MESSAGE, exception);
        }
    }

    private static Object unauthenticatedValue(AuthenticatedMemberId.Unauthenticated policy) {
        return switch (policy) {
            case THROW -> throw new AuthenticationCredentialsNotFoundException(AUTHENTICATION_REQUIRED_MESSAGE);
            case NULL -> null;
        };
    }
}

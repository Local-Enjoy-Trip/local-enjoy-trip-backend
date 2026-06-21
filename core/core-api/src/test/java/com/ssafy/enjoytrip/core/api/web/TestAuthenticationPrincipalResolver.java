package com.ssafy.enjoytrip.core.api.web;

import java.security.Principal;

import com.ssafy.enjoytrip.core.api.security.AuthenticatedUserId;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
class TestAuthenticationPrincipalResolver implements HandlerMethodArgumentResolver {
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
        Principal principal = webRequest.getUserPrincipal();
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return unauthenticatedValue(annotation.unauthenticated());
        }
        return principal.getName().strip();
    }

    private static String unauthenticatedValue(AuthenticatedUserId.Unauthenticated policy) {
        return switch (policy) {
            case THROW -> throw new AuthenticationCredentialsNotFoundException(AUTHENTICATION_REQUIRED_MESSAGE);
            case NULL -> null;
        };
    }
}

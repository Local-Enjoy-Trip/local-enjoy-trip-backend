package com.ssafy.enjoytrip.web;

import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.security.Principal;

public class TestAuthenticationPrincipalResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Principal principal = webRequest.getUserPrincipal();
        Jwt jwt = null;
        if (principal instanceof JwtAuthenticationToken jwtAuth) {
            jwt = jwtAuth.getToken();
        } else if (principal instanceof Authentication auth) {
            Object p = auth.getPrincipal();
            if (p instanceof Jwt) {
                jwt = (Jwt) p;
            }
        } else if (principal instanceof Jwt) {
            jwt = (Jwt) principal;
        }

        if (jwt == null) {
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            String method = request != null ? request.getMethod() : "GET";
            String path = request != null ? request.getRequestURI() : "";
            if (requiresAuthentication(method, path)) {
                throw new CoreException(ErrorType.AUTHENTICATION_REQUIRED);
            }
        }
        return jwt;
    }

    private boolean requiresAuthentication(String method, String path) {
        if (path == null) return false;

        if ("GET".equalsIgnoreCase(method)) {
            return path.equals("/api/members/me")
                    || path.equals("/api/map/explore");
        }
        if ("POST".equalsIgnoreCase(method)) {
            return path.equals("/api/attraction-tags")
                    || path.equals("/api/plans")
                    || path.equals("/api/note-images/presigned-upload");
        }
        if ("PUT".equalsIgnoreCase(method)) {
            return path.startsWith("/api/members/")
                    || (path.startsWith("/api/attractions/")
                            && (path.endsWith("/favorite")
                                    || path.endsWith("/rating")
                                    || path.endsWith("/tags")))
                    || path.startsWith("/api/attraction-tags/")
                    || path.startsWith("/api/plans/");
        }
        if ("DELETE".equalsIgnoreCase(method)) {
            return path.startsWith("/api/members/")
                    || (path.startsWith("/api/attractions/")
                            && (path.endsWith("/favorite") || path.endsWith("/rating")))
                    || path.startsWith("/api/attraction-tags/")
                    || path.startsWith("/api/plans/");
        }
        return false;
    }
}

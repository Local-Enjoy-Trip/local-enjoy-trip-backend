package com.ssafy.enjoytrip.web.controller;

import static com.ssafy.enjoytrip.support.error.ErrorType.AUTHENTICATION_REQUIRED;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.MapExploreResult;
import com.ssafy.enjoytrip.service.MapExploreService;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.api.MapApi;
import com.ssafy.enjoytrip.web.dto.request.MapExploreRequest;
import com.ssafy.enjoytrip.web.dto.response.MapExploreResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController implements MapApi {
    private final MapExploreService service;

    @GetMapping("/explore")
    @Override
    public ApiResponse<MapExploreResponse> explore(@Valid @ModelAttribute MapExploreRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        MapExploreResult result = service.explore(request.toCommand(authenticatedUserId(jwt)));

        return success(MapExploreResponse.from(result));
    }

    private static String authenticatedUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new CoreException(AUTHENTICATION_REQUIRED);
        }

        return jwt.getSubject().trim();
    }
}

package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.service.MapExploreService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.MapApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.MapExploreRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.MapExploreResponse;
import com.ssafy.enjoytrip.core.domain.MapExploreResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.ssafy.enjoytrip.core.api.security.AuthenticatedUserId;
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
                                                   @AuthenticatedUserId String authenticatedUserId) {
        MapExploreResult result = service.explore(
                authenticatedUserId,
                request.requiredLongitude(),
                request.requiredLatitude(),
                request.normalizedRadiusMeters(),
                request.normalizedFilter(),
                request.noteCategory()
        );

        return success(MapExploreResponse.from(result));
    }
}

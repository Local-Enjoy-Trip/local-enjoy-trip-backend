package com.ssafy.enjoytrip.web.controller;

import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.MapExploreResult;
import com.ssafy.enjoytrip.service.MapExploreService;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.api.MapApi;
import com.ssafy.enjoytrip.web.dto.request.MapExploreRequest;
import com.ssafy.enjoytrip.web.dto.response.MapExploreResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.ssafy.enjoytrip.web.security.AuthenticatedUserId;
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
        MapExploreResult result = service.explore(request.toCommand(authenticatedUserId));

        return success(MapExploreResponse.from(result));
    }
}

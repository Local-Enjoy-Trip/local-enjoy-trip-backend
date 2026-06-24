package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId;
import com.ssafy.enjoytrip.core.api.web.api.MapApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.MapExploreRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MapSearchRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.MapExploreResponse;
import com.ssafy.enjoytrip.core.domain.MapExploreResult;
import com.ssafy.enjoytrip.core.domain.MapPin;
import com.ssafy.enjoytrip.core.domain.service.MapExploreService;
import com.ssafy.enjoytrip.core.domain.service.MapSearchService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController implements MapApi {
    private final MapExploreService service;
    private final MapSearchService mapSearchService;

    @GetMapping("/explore")
    @Override
    public ApiResponse<MapExploreResponse> explore(@Valid @ModelAttribute MapExploreRequest request,
                                                   @AuthenticatedMemberId Long memberId) {
        MapExploreResult result = service.explore(
                memberId,
                request.requiredLongitude(),
                request.requiredLatitude(),
                request.normalizedRadiusMeters(),
                request.normalizedFilter(),
                request.noteCategory()
        );

        return success(MapExploreResponse.from(result));
    }

    @GetMapping("/search")
    @Override
    public ApiResponse<List<MapPin>> search(@Valid @ModelAttribute MapSearchRequest request,
                                            @AuthenticatedMemberId Long memberId) {
        List<MapPin> resultList = mapSearchService.search(
                request.requiredKeyword(),
                request.requiredLongitude(),
                request.requiredLatitude(),
                request.radius(),
                request.normalizedTarget(),
                request.noteCategory(),
                request.cappedLimit(),
                memberId
        );
        return success(resultList);
    }
}

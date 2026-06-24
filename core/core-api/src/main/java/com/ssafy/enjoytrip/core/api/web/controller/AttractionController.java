package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId.Unauthenticated.NULL;
import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.PopularAttractionResult;
import com.ssafy.enjoytrip.core.domain.service.AttractionService;
import com.ssafy.enjoytrip.core.domain.service.AttractionStatsService;
import com.ssafy.enjoytrip.core.support.error.ErrorCode;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.AttractionApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.AttractionSearchRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.NearbySectionRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.RatingRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.AttractionDetailResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.AttractionStatsResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.AttractionsResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.PopularAttractionsResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attractions")
@RequiredArgsConstructor
public class AttractionController implements AttractionApi {
    private final AttractionService service;
    private final AttractionStatsService statsService;

    @GetMapping
    @Override
    public ApiResponse<AttractionsResponse> search(
            @ModelAttribute AttractionSearchRequest request,
            @AuthenticatedMemberId(unauthenticated = NULL) Long memberId
    ) {
        List<Attraction> attractions = service.searchAttractions(
                request.toCondition(),
                memberId
        );

        return success(new AttractionsResponse(attractions));
    }

    @GetMapping("/popular-nearby")
    @Override
    public ApiResponse<PopularAttractionsResponse> popularNearby(
            @Valid @ModelAttribute NearbySectionRequest request,
            @AuthenticatedMemberId(unauthenticated = NULL) Long memberId
    ) {
        List<PopularAttractionResult> attractions = service.findPopularNearbyAttractions(
                request.toCondition(),
                memberId
        );

        return success(PopularAttractionsResponse.from(attractions));
    }

    @GetMapping("/{id}")
    @Override
    public ApiResponse<AttractionDetailResponse> detail(
            @PathVariable Long id,
            @AuthenticatedMemberId(unauthenticated = NULL) Long memberId
    ) {
        Attraction attraction = service.findAttractionDetail(id, memberId);

        return success(new AttractionDetailResponse(attraction));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @Override
    public ApiResponse<Void> rejectPost() {
        return ApiResponse.fail(ErrorCode.C405, "GET /api/attractions를 사용하세요.");
    }

    @PutMapping("/{id}/save")
    @Override
    public ApiResponse<Void> save(
            @PathVariable Long id,
            @AuthenticatedMemberId Long memberId
    ) {
        service.addSave(id, memberId);

        return success();
    }

    @DeleteMapping("/{id}/save")
    @Override
    public ApiResponse<Void> unsave(
            @PathVariable Long id,
            @AuthenticatedMemberId Long memberId
    ) {
        service.removeSave(id, memberId);

        return success();
    }

    @PutMapping("/{id}/rating")
    @Override
    public ApiResponse<Void> rate(@PathVariable Long id,
                                  @Valid @RequestBody RatingRequest request,
                                  @AuthenticatedMemberId Long memberId) {
        service.upsertRating(id, memberId, request.rating());

        return success();
    }

    @DeleteMapping("/{id}/rating")
    @Override
    public ApiResponse<Void> deleteRating(
            @PathVariable Long id,
            @AuthenticatedMemberId Long memberId
    ) {
        service.removeRating(id, memberId);

        return success();
    }

    @GetMapping("/{id}/stats")
    @Override
    public ApiResponse<AttractionStatsResponse> stats(
            @PathVariable Long id,
            @AuthenticatedMemberId(unauthenticated = NULL) Long memberId
    ) {
        return success(new AttractionStatsResponse(
                statsService.findStats(id, memberId)
        ));
    }

}

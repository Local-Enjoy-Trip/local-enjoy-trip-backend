package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.ATTRACTION_NOT_FOUND;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.ATTRACTIONS_POST_NOT_ALLOWED;
import static com.ssafy.enjoytrip.core.api.security.AuthenticatedUserId.Unauthenticated.BLANK;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_ID;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.TAG_NOT_FOUND;
import static com.ssafy.enjoytrip.core.support.response.ApiResponse.fail;
import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.PopularAttraction;
import com.ssafy.enjoytrip.core.domain.service.AttractionService;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.AttractionApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.AttractionTagsRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.AttractionSearchRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.NearbySectionRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.RatingRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.AttractionStatsResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.AttractionsResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.PopularAttractionsResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import com.ssafy.enjoytrip.core.api.security.AuthenticatedUserId;
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

    @GetMapping
    @Override
    public ApiResponse<AttractionsResponse> search(
            @ModelAttribute AttractionSearchRequest request,
            @AuthenticatedUserId(unauthenticated = BLANK) String authenticatedUserId
    ) {
        List<Attraction> attractions = service.searchAttractions(
                request.toCondition(),
                authenticatedUserId
        );

        return success(new AttractionsResponse(attractions));
    }

    @GetMapping("/popular-nearby")
    @Override
    public ApiResponse<PopularAttractionsResponse> popularNearby(
            @Valid @ModelAttribute NearbySectionRequest request,
            @AuthenticatedUserId(unauthenticated = BLANK) String authenticatedUserId
    ) {
        List<PopularAttraction> attractions = service.findPopularNearbyAttractions(
                request.toCondition(),
                authenticatedUserId
        );

        return success(PopularAttractionsResponse.from(attractions));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @Override
    public ApiResponse<Void> rejectPost() {
        return fail(ATTRACTIONS_POST_NOT_ALLOWED);
    }

    @PutMapping("/{id}/favorite")
    @Override
    public ApiResponse<Void> favorite(@PathVariable Long id, @AuthenticatedUserId String authenticatedUserId) {
        requireAttraction(id);
        service.addFavorite(id, authenticatedUserId);

        return success();
    }

    @DeleteMapping("/{id}/favorite")
    @Override
    public ApiResponse<Void> unfavorite(@PathVariable Long id, @AuthenticatedUserId String authenticatedUserId) {
        requireAttraction(id);
        service.removeFavorite(id, authenticatedUserId);

        return success();
    }

    @PutMapping("/{id}/rating")
    @Override
    public ApiResponse<Void> rate(@PathVariable Long id,
                                  @Valid @RequestBody RatingRequest request,
                                  @AuthenticatedUserId String authenticatedUserId) {
        requireAttraction(id);
        service.upsertRating(id, authenticatedUserId, request.rating());

        return success();
    }

    @DeleteMapping("/{id}/rating")
    @Override
    public ApiResponse<Void> deleteRating(@PathVariable Long id, @AuthenticatedUserId String authenticatedUserId) {
        requireAttraction(id);
        service.removeRating(id, authenticatedUserId);

        return success();
    }

    @GetMapping("/{id}/stats")
    @Override
    public ApiResponse<AttractionStatsResponse> stats(
            @PathVariable Long id,
            @AuthenticatedUserId(unauthenticated = BLANK) String authenticatedUserId
    ) {
        requireAttraction(id);
        return success(new AttractionStatsResponse(service.findStats(id, authenticatedUserId)));
    }

    @PutMapping("/{id}/tags")
    @Override
    public ApiResponse<Void> replaceTags(
            @PathVariable Long id,
            @Valid @RequestBody AttractionTagsRequest request,
            @AuthenticatedUserId String authenticatedUserId
    ) {
        requireAttraction(id);

        if (!service.replaceTags(id, request.tagIds())) {
            throw new CoreException(TAG_NOT_FOUND);
        }

        return success();
    }

    private void requireAttraction(Long id) {
        if (id == null || id <= 0) {
            throw new CoreException(INVALID_ID);
        }

        if (!service.existsById(id)) {
            throw new CoreException(ATTRACTION_NOT_FOUND);
        }
    }


}

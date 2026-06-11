package com.ssafy.enjoytrip.web.controller;

import static com.ssafy.enjoytrip.support.error.ErrorType.ATTRACTION_NOT_FOUND;
import static com.ssafy.enjoytrip.support.error.ErrorType.ATTRACTIONS_POST_NOT_ALLOWED;
import static com.ssafy.enjoytrip.support.error.ErrorType.AUTHENTICATION_REQUIRED;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_ID;
import static com.ssafy.enjoytrip.support.error.ErrorType.TAG_NOT_FOUND;
import static com.ssafy.enjoytrip.support.response.ApiResponse.fail;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.domain.AttractionSearchCondition;
import com.ssafy.enjoytrip.domain.PopularAttraction;
import com.ssafy.enjoytrip.service.AttractionService;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.api.AttractionApi;
import com.ssafy.enjoytrip.web.dto.request.AttractionTagsRequest;
import com.ssafy.enjoytrip.web.dto.request.AttractionSearchRequest;
import com.ssafy.enjoytrip.web.dto.request.NearbySectionRequest;
import com.ssafy.enjoytrip.web.dto.request.RatingRequest;
import com.ssafy.enjoytrip.web.dto.response.AttractionStatsResponse;
import com.ssafy.enjoytrip.web.dto.response.AttractionsResponse;
import com.ssafy.enjoytrip.web.dto.response.PopularAttractionsResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attractions")
@RequiredArgsConstructor
public class AttractionController implements AttractionApi {
    private final AttractionService service;

    @GetMapping
    @Override
    public ApiResponse<AttractionsResponse> search(@ModelAttribute AttractionSearchRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        AttractionSearchCondition condition = new AttractionSearchCondition(
                request.sidoCode(),
                request.gugunCode(),
                request.contentTypeId(),
                request.keyword(),
                request.mapX(),
                request.mapY(),
                request.radius()
        );
        List<Attraction> attractions = service.searchAttractions(condition, authenticatedUserIdOrBlank(jwt));

        return success(new AttractionsResponse(attractions));
    }

    @GetMapping("/popular-nearby")
    @Override
    public ApiResponse<PopularAttractionsResponse> popularNearby(
            @Valid @ModelAttribute NearbySectionRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<PopularAttraction> attractions = service.findPopularNearbyAttractions(
                request.toCondition(),
                authenticatedUserIdOrBlank(jwt)
        );

        return success(PopularAttractionsResponse.from(attractions));
    }

    @PostMapping
    @Override
    public ResponseEntity<ApiResponse<Void>> rejectPost() {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(fail(ATTRACTIONS_POST_NOT_ALLOWED));
    }

    @PutMapping("/{id}/favorite")
    @Override
    public ApiResponse<Void> favorite(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        requireAttraction(id);
        service.addFavorite(id, authenticatedUserId(jwt));

        return success();
    }

    @DeleteMapping("/{id}/favorite")
    @Override
    public ApiResponse<Void> unfavorite(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        requireAttraction(id);
        service.removeFavorite(id, authenticatedUserId(jwt));

        return success();
    }

    @PutMapping("/{id}/rating")
    @Override
    public ApiResponse<Void> rate(@PathVariable Long id,
                                  @Valid @RequestBody RatingRequest request,
                                  @AuthenticationPrincipal Jwt jwt) {
        requireAttraction(id);
        service.upsertRating(id, authenticatedUserId(jwt), request.rating());

        return success();
    }

    @DeleteMapping("/{id}/rating")
    @Override
    public ApiResponse<Void> deleteRating(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        requireAttraction(id);
        service.removeRating(id, authenticatedUserId(jwt));

        return success();
    }

    @GetMapping("/{id}/stats")
    @Override
    public ApiResponse<AttractionStatsResponse> stats(@PathVariable Long id,
                                                      @AuthenticationPrincipal Jwt jwt) {
        requireAttraction(id);
        return success(new AttractionStatsResponse(service.findStats(id, authenticatedUserIdOrBlank(jwt))));
    }

    @PutMapping("/{id}/tags")
    @Override
    public ApiResponse<Void> replaceTags(@PathVariable Long id,
                                         @Valid @RequestBody AttractionTagsRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        authenticatedUserId(jwt);
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

    private static String authenticatedUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new CoreException(AUTHENTICATION_REQUIRED);
        }

        return trim(jwt.getSubject());
    }

    private static String authenticatedUserIdOrBlank(Jwt jwt) {
        if (jwt == null) {
            return "";
        }

        return trim(jwt.getSubject());
    }

    private static String trim(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }

}

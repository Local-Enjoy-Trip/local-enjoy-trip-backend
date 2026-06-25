package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId.Unauthenticated.NULL;
import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId;
import com.ssafy.enjoytrip.core.api.web.api.CourseApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseCreateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseFeedRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseOrderRecommendationRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CoursePopularFeedRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseRecommendationRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.aicourse.AiCourseGenerateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.AiCoursePreviewResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.CourseFeedResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.CourseResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.CoursesResponse;
import com.ssafy.enjoytrip.core.domain.AiCoursePreview;
import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseOrderOptimizationContext;
import com.ssafy.enjoytrip.core.domain.service.AiCourseGenerationService;
import com.ssafy.enjoytrip.core.domain.service.CourseService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController implements CourseApi {
    private final CourseService courseService;
    private final AiCourseGenerationService aiCourseGenerationService;

    @GetMapping("/feed")
    @Override
    public ApiResponse<CourseFeedResponse> feed(@ModelAttribute CourseFeedRequest request) {
        return success(CourseFeedResponse.from(courseService.findPublicFeed(request.toCondition())));
    }

    @GetMapping("/recommendations")
    @Override
    public ApiResponse<CourseFeedResponse> recommendations(
            @ModelAttribute CourseRecommendationRequest request,
            @AuthenticatedMemberId Long authenticatedMemberId) {
        return success(CourseFeedResponse.from(
                courseService.findRecommendations(
                        authenticatedMemberId,
                        request.normalizedRegionName(),
                        request.resolvedLimit()
                )
        ));
    }

    @GetMapping("/feed/popular")
    @Override
    public ApiResponse<CourseFeedResponse> popularFeed(@ModelAttribute CoursePopularFeedRequest request) {
        return success(CourseFeedResponse.from(
                courseService.findPopularByRegion(request.normalizedRegionName(), request.resolvedLimit())
        ));
    }

    @GetMapping("/{id}")
    @Override
    public ApiResponse<CourseResponse> detail(
            @PathVariable String id,
            @AuthenticatedMemberId(unauthenticated = AuthenticatedMemberId.Unauthenticated.NULL) Long authenticatedMemberId
    ) {
        Course course = courseService.view(id.strip(), authenticatedMemberId);
        return success(CourseResponse.from(course));
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<CoursesResponse> mine(@AuthenticatedMemberId Long authenticatedMemberId) {
        List<CourseResponse> courses = courseService.findMyCourses(authenticatedMemberId).stream()
                .map(CourseResponse::from)
                .toList();
        return success(new CoursesResponse(courses));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ApiResponse<CourseResponse> create(@RequestBody CourseCreateRequest request,
                                              @AuthenticatedMemberId Long authenticatedMemberId) {
        Course created = courseService.createCourse(request.toCourse(authenticatedMemberId));
        return success(CourseResponse.from(created));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ApiResponse<CourseResponse> update(@PathVariable String id,
                                              @RequestBody CourseUpdateRequest request,
                                              @AuthenticatedMemberId Long authenticatedMemberId) {
        Course updated = courseService.updateCourse(
                authenticatedMemberId,
                request.toCourse(id.strip(), authenticatedMemberId)
        );
        return success(CourseResponse.from(updated));
    }

    @PostMapping("/{id}/order-recommendation")
    @Override
    public ApiResponse<CourseResponse> recommendOrder(@PathVariable String id,
                                                      @RequestBody(required = false)
                                                      CourseOrderRecommendationRequest request,
                                                      @AuthenticatedMemberId Long authenticatedMemberId) {
        Course recommended = courseService.recommendCourseOrder(
                authenticatedMemberId,
                id.strip(),
                toContext(request)
        );
        return success(CourseResponse.from(recommended));
    }

    private static CourseOrderOptimizationContext toContext(CourseOrderRecommendationRequest request) {
        return request == null ? CourseOrderOptimizationContext.empty() : request.toContext();
    }

    @PostMapping("/{id}/save")
    @Override
    public ApiResponse<Void> save(@PathVariable String id,
                                  @AuthenticatedMemberId Long authenticatedMemberId) {
        courseService.saveCourse(authenticatedMemberId, id.strip());
        return success();
    }

    @DeleteMapping("/{id}/save")
    @Override
    public ApiResponse<Void> unsave(@PathVariable String id,
                                    @AuthenticatedMemberId Long authenticatedMemberId) {
        courseService.unsaveCourse(authenticatedMemberId, id.strip());
        return success();
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable String id,
                                    @AuthenticatedMemberId Long authenticatedMemberId) {
        courseService.deleteCourse(authenticatedMemberId, id.strip());
        return success();
    }

    @PostMapping(value = "/ai-generate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ApiResponse<AiCoursePreviewResponse> generateAiCourse(
            @RequestBody @Valid AiCourseGenerateRequest request,
            @AuthenticatedMemberId Long authenticatedMemberId
    ) {
        List<String> themeLabels = request.themes().stream()
                .map(theme -> theme.getLabel())
                .toList();

        AiCoursePreview preview = aiCourseGenerationService.generatePreview(
                authenticatedMemberId,
                request.regionName(),
                request.companion().getLabel(),
                themeLabels,
                request.pace().getLabel(),
                request.pace().getPlaceCount()
        );

        return success(AiCoursePreviewResponse.from(preview));
    }
}

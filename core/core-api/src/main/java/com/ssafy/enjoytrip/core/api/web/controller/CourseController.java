package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseCreateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseFeedRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseMdFeedRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseOrderRecommendationRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CoursePopularFeedRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.CourseFeedResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.CourseResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.CoursesResponse;
import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseOrderOptimizationContext;
import com.ssafy.enjoytrip.core.domain.service.CourseService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
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
@Validated
public class CourseController {
    private final CourseService courseService;

    @GetMapping("/feed")
    public ApiResponse<CourseFeedResponse> feed(@Valid @ModelAttribute CourseFeedRequest request) {
        return success(CourseFeedResponse.from(courseService.findPublicFeed(request.toCondition())));
    }

    @GetMapping("/feed/md")
    public ApiResponse<CourseFeedResponse> mdFeed(@Valid @ModelAttribute CourseMdFeedRequest request) {
        return success(CourseFeedResponse.from(
                courseService.findMdFeed(request.mapX(), request.mapY(), request.resolvedLimit())
        ));
    }

    @GetMapping("/feed/popular")
    public ApiResponse<CourseFeedResponse> popularFeed(@Valid @ModelAttribute CoursePopularFeedRequest request) {
        return success(CourseFeedResponse.from(
                courseService.findPopularByRegion(request.normalizedRegionName(), request.resolvedLimit())
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> detail(@PathVariable @NotBlank String id) {
        return success(CourseResponse.from(courseService.findPublicRequired(id.strip())));
    }

    @GetMapping("/me")
    public ApiResponse<CoursesResponse> mine(@AuthenticatedMemberId Long authenticatedMemberId) {
        List<CourseResponse> courses = courseService.findMyCourses(authenticatedMemberId).stream()
                .map(CourseResponse::from)
                .toList();
        return success(new CoursesResponse(courses));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<CourseResponse> create(@Valid @RequestBody CourseCreateRequest request,
                                              @AuthenticatedMemberId Long authenticatedMemberId) {
        Course created = courseService.createCourse(request.toCourse(authenticatedMemberId));
        return success(CourseResponse.from(created));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<CourseResponse> update(@PathVariable @NotBlank String id,
                                              @Valid @RequestBody CourseUpdateRequest request,
                                              @AuthenticatedMemberId Long authenticatedMemberId) {
        Course updated = courseService.updateCourse(
                authenticatedMemberId,
                request.toCourse(id.strip(), authenticatedMemberId)
        );
        return success(CourseResponse.from(updated));
    }

    @PostMapping("/{id}/order-recommendation")
    public ApiResponse<CourseResponse> recommendOrder(@PathVariable @NotBlank String id,
                                                      @Valid @RequestBody(required = false)
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
    public ApiResponse<Void> save(@PathVariable @NotBlank String id,
                                  @AuthenticatedMemberId Long authenticatedMemberId) {
        courseService.saveCourse(authenticatedMemberId, id.strip());
        return success();
    }

    @DeleteMapping("/{id}/save")
    public ApiResponse<Void> unsave(@PathVariable @NotBlank String id,
                                    @AuthenticatedMemberId Long authenticatedMemberId) {
        courseService.unsaveCourse(authenticatedMemberId, id.strip());
        return success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable @NotBlank String id,
                                    @AuthenticatedMemberId Long authenticatedMemberId) {
        courseService.deleteCourse(authenticatedMemberId, id.strip());
        return success();
    }
}

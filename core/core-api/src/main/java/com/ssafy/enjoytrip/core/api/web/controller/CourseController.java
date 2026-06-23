package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.api.security.AuthenticatedUserId;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseCreateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseFeedRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseOrderRecommendationRequest;
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

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> detail(@PathVariable @NotBlank String id) {
        return success(CourseResponse.from(courseService.findPublicRequired(id.strip())));
    }

    @GetMapping("/me")
    public ApiResponse<CoursesResponse> mine(@AuthenticatedUserId String authenticatedUserId) {
        List<CourseResponse> courses = courseService.findMyCourses(authenticatedUserId).stream()
                .map(CourseResponse::from)
                .toList();
        return success(new CoursesResponse(courses));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<CourseResponse> create(@Valid @RequestBody CourseCreateRequest request,
                                              @AuthenticatedUserId String authenticatedUserId) {
        Course created = courseService.createCourse(request.toCourse(authenticatedUserId));
        return success(CourseResponse.from(created));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<CourseResponse> update(@PathVariable @NotBlank String id,
                                              @Valid @RequestBody CourseUpdateRequest request,
                                              @AuthenticatedUserId String authenticatedUserId) {
        Course updated = courseService.updateCourse(
                authenticatedUserId,
                request.toCourse(id.strip(), authenticatedUserId)
        );
        return success(CourseResponse.from(updated));
    }

    @PostMapping("/{id}/order-recommendation")
    public ApiResponse<CourseResponse> recommendOrder(@PathVariable @NotBlank String id,
                                                      @Valid @RequestBody(required = false)
                                                      CourseOrderRecommendationRequest request,
                                                      @AuthenticatedUserId String authenticatedUserId) {
        Course recommended = courseService.recommendCourseOrder(
                authenticatedUserId,
                id.strip(),
                toContext(request)
        );
        return success(CourseResponse.from(recommended));
    }

    private static CourseOrderOptimizationContext toContext(CourseOrderRecommendationRequest request) {
        return request == null ? CourseOrderOptimizationContext.empty() : request.toContext();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable @NotBlank String id,
                                    @AuthenticatedUserId String authenticatedUserId) {
        courseService.deleteCourse(authenticatedUserId, id.strip());
        return success();
    }
}

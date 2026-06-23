package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Course;
import java.util.List;

public record CourseFeedResponse(
        List<CourseResponse> courses
) {
    public static CourseFeedResponse from(List<Course> courses) {
        return new CourseFeedResponse(
                courses.stream()
                        .map(CourseResponse::from)
                        .toList()
        );
    }
}

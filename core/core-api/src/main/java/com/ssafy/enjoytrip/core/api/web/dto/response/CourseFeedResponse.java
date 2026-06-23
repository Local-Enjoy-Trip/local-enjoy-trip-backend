package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.CourseFeedSection;
import java.util.List;

public record CourseFeedResponse(
        List<CourseFeedSectionResponse> sections
) {
    public static CourseFeedResponse from(List<CourseFeedSection> sections) {
        return new CourseFeedResponse(
                sections.stream()
                        .map(CourseFeedSectionResponse::from)
                        .toList()
        );
    }

    public record CourseFeedSectionResponse(
            String key,
            String label,
            String sort,
            List<CourseResponse> courses
    ) {
        static CourseFeedSectionResponse from(CourseFeedSection section) {
            return new CourseFeedSectionResponse(
                    section.key(),
                    section.label(),
                    section.sort(),
                    section.courses().stream().map(CourseResponse::from).toList()
            );
        }
    }
}

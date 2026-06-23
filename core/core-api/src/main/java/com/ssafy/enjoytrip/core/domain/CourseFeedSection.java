package com.ssafy.enjoytrip.core.domain;

import java.util.List;

public record CourseFeedSection(
        String key,
        String label,
        String sort,
        List<Course> courses
) {
}

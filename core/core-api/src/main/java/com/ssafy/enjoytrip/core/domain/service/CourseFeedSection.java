package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.Course;
import java.util.List;

public record CourseFeedSection(
        String key,
        String label,
        String sort,
        List<Course> courses
) {
}

package com.ssafy.enjoytrip.core.domain;

public interface CourseOrderOptimizer {
    default Course recommend(Course course) {
        return recommend(course, CourseOrderOptimizationContext.empty());
    }

    Course recommend(Course course, CourseOrderOptimizationContext context);
}

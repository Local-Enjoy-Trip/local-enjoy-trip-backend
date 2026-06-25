package com.ssafy.enjoytrip.core.domain;

public record CourseRecommendationCandidate(
        Course course,
        String dominantCategory,
        double similarityDistance
) {}

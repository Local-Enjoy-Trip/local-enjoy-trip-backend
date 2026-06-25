package com.ssafy.enjoytrip.external.courseembedding;

public record CourseEmbeddingInput(
        String courseId,
        String title,
        String regionName,
        String tagNames,
        String stopTitles
) {}

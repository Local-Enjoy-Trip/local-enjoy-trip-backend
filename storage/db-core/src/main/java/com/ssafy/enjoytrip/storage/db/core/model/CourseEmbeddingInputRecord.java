package com.ssafy.enjoytrip.storage.db.core.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseEmbeddingInputRecord {
    private String courseId;
    private String title;
    private String regionName;
    private String tagNames;
    private String stopTitles;
    private String dominantCategory;
}

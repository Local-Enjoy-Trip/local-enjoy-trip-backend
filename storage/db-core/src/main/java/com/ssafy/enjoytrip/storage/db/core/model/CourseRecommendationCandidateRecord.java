package com.ssafy.enjoytrip.storage.db.core.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseRecommendationCandidateRecord {
    private String id;
    private Long ownerMemberId;
    private String title;
    private String regionName;
    private String date;
    private Integer saveCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String dominantCategory;
    private Double similarityDistance;
}

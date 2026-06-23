package com.ssafy.enjoytrip.storage.db.core.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseRouteSegmentRecord {
    private String courseId;

    private Long fromCourseItemId;

    private Long toCourseItemId;

    private Integer segmentOrder;

    private String travelMode;

    private Integer durationSeconds;

    private Integer distanceMeters;

    public CourseRouteSegmentRecord(String courseId,
                                    Long fromCourseItemId,
                                    Long toCourseItemId,
                                    Integer segmentOrder,
                                    String travelMode,
                                    Integer durationSeconds,
                                    Integer distanceMeters) {
        this.courseId = courseId;
        this.fromCourseItemId = fromCourseItemId;
        this.toCourseItemId = toCourseItemId;
        this.segmentOrder = segmentOrder;
        this.travelMode = travelMode;
        this.durationSeconds = durationSeconds;
        this.distanceMeters = distanceMeters;
    }
}

package com.ssafy.enjoytrip.storage.db.core.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseItemRecord {
    private Long id;

    private String courseId;

    private String itemType;

    private Long attractionId;

    private Long noteId;

    private Integer position;

    private Integer day;

    private String memo;

    private Integer stayMinutes;

    private LocalDateTime createdAt;

    public CourseItemRecord(String courseId,
                            String itemType,
                            Long attractionId,
                            Long noteId,
                            Integer position,
                            Integer day,
                            String memo,
                            Integer stayMinutes) {
        this.courseId = courseId;
        this.itemType = itemType;
        this.attractionId = attractionId;
        this.noteId = noteId;
        this.position = position;
        this.day = day;
        this.memo = memo;
        this.stayMinutes = stayMinutes;
    }
}

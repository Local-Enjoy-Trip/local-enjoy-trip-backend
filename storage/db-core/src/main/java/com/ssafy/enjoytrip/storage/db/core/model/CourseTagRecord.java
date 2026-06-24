package com.ssafy.enjoytrip.storage.db.core.model;

public record CourseTagRecord(
        String courseId,
        Long tagId,
        String tagName
) {
}

package com.ssafy.enjoytrip.storage.db.core.model;

import java.time.LocalDateTime;

public record CourseItemDetailRecord(
        Long id,
        String courseId,
        String itemType,
        Long attractionId,
        Long noteId,
        Integer position,
        Integer day,
        String memo,
        Integer stayMinutes,
        String attractionTitle,
        String noteTitle,
        String itemTitle,
        Long noteAuthorMemberId,
        LocalDateTime createdAt
) {
}

package com.ssafy.enjoytrip.storage.db.core.model;

import java.time.LocalDateTime;

public record AttractionAdminRecord(
        Long id,
        String title,
        String addr1,
        String addr2,
        String firstImage,
        String firstImage2,
        Integer sidoCode,
        Integer gugunCode,
        Double latitude,
        Double longitude,
        String contentTypeId,
        String overview,
        String status,
        Long duplicateOfAttractionId,
        String duplicateReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
}

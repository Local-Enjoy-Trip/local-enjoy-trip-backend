package com.ssafy.enjoytrip.storage.db.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NoteMapPinRecord(
        Long id,
        String title,
        String category,
        String visibility,
        BigDecimal latitude,
        BigDecimal longitude,
        String regionName,
        String imageObjectKey,
        Long authorMemberId,
        String authorNickname,
        String authorProfileImageUrl,
        String relationship,
        LocalDateTime createdAt,
        double distanceMeters
) {
}

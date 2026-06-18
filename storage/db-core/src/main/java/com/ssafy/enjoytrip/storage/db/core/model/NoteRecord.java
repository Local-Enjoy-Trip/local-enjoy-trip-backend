package com.ssafy.enjoytrip.storage.db.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NoteRecord {
    private Long id;
    private String authorUserId;
    private String title;
    private String content;
    private String category;
    private String visibility;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String regionName;
    private String imageObjectKey;
    private String imageUrl;
    private String imageContentType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}

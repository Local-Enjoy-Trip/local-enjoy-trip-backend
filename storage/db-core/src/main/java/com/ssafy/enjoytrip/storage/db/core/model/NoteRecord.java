package com.ssafy.enjoytrip.storage.db.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoteRecord extends BaseRecord {
    private Long id;

    private Long authorMemberId;

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

    private LocalDateTime deletedAt;

    public NoteRecord(Long authorMemberId,
                      String title,
                      String content,
                      String category,
                      String visibility,
                      BigDecimal latitude,
                      BigDecimal longitude,
                      String regionName,
                      String imageObjectKey,
                      String imageUrl,
                      String imageContentType) {
        this(null, authorMemberId, title, content, category, visibility, latitude, longitude, regionName,
                imageObjectKey, imageUrl, imageContentType);
    }

    public NoteRecord(Long id,
                      Long authorMemberId,
                      String title,
                      String content,
                      String category,
                      String visibility,
                      BigDecimal latitude,
                      BigDecimal longitude,
                      String regionName,
                      String imageObjectKey,
                      String imageUrl,
                      String imageContentType) {
        this.id = id;
        this.authorMemberId = authorMemberId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.visibility = visibility;
        this.latitude = latitude;
        this.longitude = longitude;
        this.regionName = regionName;
        this.imageObjectKey = imageObjectKey;
        this.imageUrl = imageUrl;
        this.imageContentType = imageContentType;
    }
}

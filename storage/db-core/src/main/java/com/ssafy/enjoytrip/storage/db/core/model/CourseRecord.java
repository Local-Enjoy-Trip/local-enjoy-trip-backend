package com.ssafy.enjoytrip.storage.db.core.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseRecord {
    private String id;

    private Long ownerMemberId;

    private String title;

    private String regionName;

    private String visibility;

    private String status;

    private String description;

    private String coverImageUrl;

    private String curationSection;

    private Integer curationOrder;

    private Boolean createdByAdmin;

    private Double startLatitude;

    private Double startLongitude;

    private Double distanceMeters;

    private Integer saveCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public CourseRecord(String id,
                        Long ownerMemberId,
                        String title,
                        String regionName,
                        String visibility,
                        String status,
                        String description,
                        String coverImageUrl,
                        String curationSection,
                        Integer curationOrder) {
        this.id = id;
        this.ownerMemberId = ownerMemberId;
        this.title = title;
        this.regionName = regionName;
        this.visibility = visibility;
        this.status = status;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.curationSection = curationSection;
        this.curationOrder = curationOrder;
    }
}

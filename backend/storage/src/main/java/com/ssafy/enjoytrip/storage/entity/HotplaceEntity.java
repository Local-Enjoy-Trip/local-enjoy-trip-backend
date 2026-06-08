package com.ssafy.enjoytrip.storage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "hotplaces")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HotplaceEntity {
    @Id
    @Column(length = 128)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 64)
    private String type;

    @Column(name = "visit_date", nullable = false, length = 32)
    private String visitDate;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String photo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public HotplaceEntity(String id, String userId, String title, String type, String visitDate,
                          Double lat, Double lng, String description, String photo) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.type = type;
        this.visitDate = visitDate;
        this.lat = lat;
        this.lng = lng;
        this.description = description;
        this.photo = photo;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

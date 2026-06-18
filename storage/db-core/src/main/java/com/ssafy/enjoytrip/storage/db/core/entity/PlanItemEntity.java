package com.ssafy.enjoytrip.storage.db.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plan_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanItemEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false, length = 128)
    private String planId;

    @Column(name = "attraction_id", nullable = false)
    private Long attractionId;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private int day;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "stay_minutes", nullable = false)
    private int stayMinutes;

    public PlanItemEntity(String planId, Long attractionId, int position, int day, String memo, int stayMinutes) {
        this.planId = planId;
        this.attractionId = attractionId;
        this.position = position;
        this.day = day;
        this.memo = memo;
        this.stayMinutes = stayMinutes;
    }
}

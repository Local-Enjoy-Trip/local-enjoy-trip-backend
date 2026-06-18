package com.ssafy.enjoytrip.storage.db.core.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanItemEntity extends BaseEntity {
    private Long id;

    private String planId;

    private Long attractionId;

    private int position;

    private int day;

    private String memo;

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

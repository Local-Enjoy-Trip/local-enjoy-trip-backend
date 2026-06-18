package com.ssafy.enjoytrip.storage.db.core.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelPlanEntity extends BaseEntity {
    private String id;

    private String userId;

    private String title;

    private String startDate;

    private String endDate;

    private int budget;

    private String note;

    private String routeItemsJson;

    public TravelPlanEntity(String id, String userId, String title, String startDate, String endDate,
                            int budget, String note, String routeItemsJson) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.note = note;
        this.routeItemsJson = routeItemsJson;
    }

    public void update(String title, String startDate, String endDate, int budget, String note, String routeItemsJson) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.note = note;
        this.routeItemsJson = routeItemsJson;
    }
}

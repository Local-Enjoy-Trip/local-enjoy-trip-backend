package com.ssafy.enjoytrip.storage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelPlanEntity extends BaseEntity {
    @Id
    @Column(length = 128)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(nullable = false)
    private String title;

    @Column(name = "start_date", nullable = false, length = 32)
    private String startDate;

    @Column(name = "end_date", nullable = false, length = 32)
    private String endDate;

    @Column(nullable = false)
    private int budget;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "route_items", columnDefinition = "TEXT")
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

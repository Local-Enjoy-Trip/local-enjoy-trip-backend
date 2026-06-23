package com.ssafy.enjoytrip.storage.db.core.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HotplaceRecord extends BaseRecord {
    private String id;

    private Long memberId;

    private String title;

    private String type;

    private String visitDate;

    private Double lat;

    private Double lng;

    private String description;

    private String photo;

    public HotplaceRecord(String id, Long memberId, String title, String type, String visitDate,
                          Double lat, Double lng, String description, String photo) {
        this.id = id;
        this.memberId = memberId;
        this.title = title;
        this.type = type;
        this.visitDate = visitDate;
        this.lat = lat;
        this.lng = lng;
        this.description = description;
        this.photo = photo;
    }
}

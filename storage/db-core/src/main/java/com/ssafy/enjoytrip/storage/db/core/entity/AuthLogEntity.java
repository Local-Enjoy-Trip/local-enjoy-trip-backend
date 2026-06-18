package com.ssafy.enjoytrip.storage.db.core.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthLogEntity {
    private Long id;

    private String userId;

    private String eventType;

    private LocalDateTime loggedAt;

    public AuthLogEntity(String userId, String eventType) {
        this.userId = userId;
        this.eventType = eventType;
    }
}

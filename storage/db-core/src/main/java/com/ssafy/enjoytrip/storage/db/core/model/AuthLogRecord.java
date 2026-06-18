package com.ssafy.enjoytrip.storage.db.core.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthLogRecord {
    private Long id;

    private String userId;

    private String eventType;

    private LocalDateTime loggedAt;

    public AuthLogRecord(String userId, String eventType) {
        this.userId = userId;
        this.eventType = eventType;
    }
}

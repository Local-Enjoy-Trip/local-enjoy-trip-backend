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

    private Long memberId;

    private String eventType;

    private LocalDateTime loggedAt;

    public AuthLogRecord(Long memberId, String eventType) {
        this.memberId = memberId;
        this.eventType = eventType;
    }
}

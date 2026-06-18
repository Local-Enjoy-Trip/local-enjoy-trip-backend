package com.ssafy.enjoytrip.storage.db.core.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public abstract class BaseRecord {
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

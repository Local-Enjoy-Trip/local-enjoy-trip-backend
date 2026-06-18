package com.ssafy.enjoytrip.storage.db.core.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public abstract class BaseEntity {
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

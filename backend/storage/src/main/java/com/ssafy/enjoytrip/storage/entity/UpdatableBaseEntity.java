package com.ssafy.enjoytrip.storage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class UpdatableBaseEntity extends BaseEntity {
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

package com.ssafy.enjoytrip.storage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType;

    @CreatedDate
    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    public AuthLogEntity(String userId, String eventType) {
        this.userId = userId;
        this.eventType = eventType;
    }
}

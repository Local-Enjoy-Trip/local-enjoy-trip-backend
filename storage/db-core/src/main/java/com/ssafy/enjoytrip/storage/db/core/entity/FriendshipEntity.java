package com.ssafy.enjoytrip.storage.db.core.entity;

import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "friendships")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendshipEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_user_id", nullable = false, length = 64)
    private String requesterUserId;

    @Column(name = "addressee_user_id", nullable = false, length = 64)
    private String addresseeUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public FriendshipEntity(String requesterUserId, String addresseeUserId) {
        this.requesterUserId = requesterUserId;
        this.addresseeUserId = addresseeUserId;
        this.status = FriendshipStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    public void transitionTo(FriendshipStatus nextStatus) {
        this.status = nextStatus;
        this.respondedAt = LocalDateTime.now();
    }
}

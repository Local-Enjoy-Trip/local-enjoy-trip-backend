package com.ssafy.enjoytrip.storage.db.core.model;

import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendshipRecord extends BaseRecord {
    private Long id;

    private String requesterUserId;

    private String addresseeUserId;

    private FriendshipStatus status = FriendshipStatus.PENDING;

    private LocalDateTime requestedAt;

    private LocalDateTime respondedAt;

    public FriendshipRecord(String requesterUserId, String addresseeUserId) {
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

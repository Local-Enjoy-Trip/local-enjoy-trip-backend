package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.core.domain.NotificationOutboxStatus;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.core.domain.NotificationType;
import com.ssafy.enjoytrip.storage.db.core.model.FriendshipRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NotificationOutboxRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NotificationRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.FriendshipMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NotificationMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NotificationOutboxMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class NotificationFriendshipMapperContainerTest extends StorageContainerTestSupport {
    @Autowired
    private FriendshipMapper friendshipMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private NotificationOutboxMapper notificationOutboxMapper;

    @DisplayName("FriendshipMapper는 요청 조회와 상태 전이를 실제 DB에서 수행한다")
    @Test
    void friendshipMapperPersistsAndTransitionsFriendship() {
        String requester = uniqueId("requester");
        String addressee = uniqueId("addressee");
        seedMember(requester, requester + "@example.com");
        seedMember(addressee, addressee + "@example.com");
        FriendshipRecord record = new FriendshipRecord(requester, addressee);

        friendshipMapper.insert(record);
        record.transitionTo(FriendshipStatus.ACCEPTED);
        friendshipMapper.updateStatus(record);

        FriendshipRecord saved = friendshipMapper.findById(record.getId());

        assertThat(saved.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
        assertThat(saved.getRespondedAt()).isNotNull();
        assertThat(friendshipMapper.findByParticipantAndStatus(requester, FriendshipStatus.ACCEPTED))
                .extracting(FriendshipRecord::getId)
                .contains(record.getId());
        assertThat(friendshipMapper.findReceivedRequests(addressee, FriendshipStatus.ACCEPTED))
                .extracting(FriendshipRecord::getId)
                .contains(record.getId());
        assertThat(friendshipMapper.findSentRequests(requester, FriendshipStatus.ACCEPTED))
                .extracting(FriendshipRecord::getId)
                .contains(record.getId());
        assertThat(friendshipMapper.existsActiveBetween(
                requester,
                addressee,
                List.of(FriendshipStatus.PENDING, FriendshipStatus.ACCEPTED)
        )).isEqualTo(1);
    }

    @DisplayName("NotificationOutboxMapper는 outbox 상태 전이를 DB에 반영한다")
    @Test
    void notificationOutboxMapperPersistsAndTransitionsOutbox() {
        String recipient = uniqueId("outbox-recipient");
        seedMember(recipient, recipient + "@example.com");
        NotificationOutboxRecord outbox = new NotificationOutboxRecord(
                NotificationType.FRIEND_REQUEST_RECEIVED,
                recipient,
                NotificationReferenceType.FRIENDSHIP,
                1L,
                "{\"message\":\"hello\"}"
        );

        notificationOutboxMapper.insert(outbox);
        outbox.markProcessed();
        notificationOutboxMapper.markProcessed(outbox);
        NotificationOutboxRecord processed = notificationOutboxMapper.findById(outbox.getId());
        processed.markFailed("retry");
        notificationOutboxMapper.markFailed(processed);

        NotificationOutboxRecord failed = notificationOutboxMapper.findById(outbox.getId());

        assertThat(processed.getStatus()).isEqualTo(NotificationOutboxStatus.PROCESSED);
        assertThat(processed.getProcessedAt()).isNotNull();
        assertThat(failed.getStatus()).isEqualTo(NotificationOutboxStatus.FAILED);
        assertThat(failed.getLastError()).isEqualTo("retry");
    }

    @DisplayName("NotificationMapper는 outbox 중복 조회와 친구 요청 알림 읽음 처리를 수행한다")
    @Test
    void notificationMapperFindsAndMarksFriendRequestNotifications() {
        String requester = uniqueId("noti-requester");
        String recipient = uniqueId("noti-recipient");
        seedMember(requester, requester + "@example.com");
        seedMember(recipient, recipient + "@example.com");
        FriendshipRecord friendship = new FriendshipRecord(requester, recipient);
        friendshipMapper.insert(friendship);
        NotificationOutboxRecord outbox = new NotificationOutboxRecord(
                NotificationType.FRIEND_REQUEST_RECEIVED,
                recipient,
                NotificationReferenceType.FRIENDSHIP,
                friendship.getId(),
                "{\"message\":\"hello\"}"
        );
        notificationOutboxMapper.insert(outbox);
        NotificationRecord notification = new NotificationRecord(
                recipient,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                friendship.getId(),
                "{\"message\":\"hello\"}",
                outbox.getId()
        );

        notificationMapper.insert(notification);
        notificationMapper.markReadByReference(
                recipient,
                NotificationReferenceType.FRIENDSHIP,
                friendship.getId(),
                LocalDateTime.now()
        );
        NotificationRecord saved = notificationMapper.findByOutboxEventId(outbox.getId());
        saved.markRead();
        notificationMapper.updateReadAt(saved);

        assertThat(notificationMapper.existsByOutboxEventId(outbox.getId())).isEqualTo(1);
        assertThat(notificationMapper.existsUnreadFriendRequest(
                recipient,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                FriendshipStatus.PENDING
        )).isZero();
        assertThat(notificationMapper.findUnreadFriendRequests(
                recipient,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                FriendshipStatus.PENDING,
                10
        )).isEmpty();
        assertThat(notificationMapper.findByOutboxEventId(outbox.getId()).getReadAt()).isNotNull();
    }
}

package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.core.domain.NotificationType;
import com.ssafy.enjoytrip.storage.db.core.model.FriendshipRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NotificationRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.FriendshipMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NotificationMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class NotificationFriendshipMapperH2Test extends H2MapperTestSupport {
    @Autowired
    private FriendshipMapper friendshipMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @DisplayName("FriendshipMapper는 H2 인메모리 DB에서 요청 조회와 상태 전이를 수행한다")
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

    @DisplayName("NotificationMapper는 outbox 없이 비즈니스 키로 친구 요청 알림 중복을 막는다")
    @Test
    void notificationMapperPreventsDuplicateFriendRequestNotificationsByBusinessKey() {
        String requester = uniqueId("noti-requester");
        String recipient = uniqueId("noti-recipient");
        seedMember(requester, requester + "@example.com");
        seedMember(recipient, recipient + "@example.com");
        FriendshipRecord friendship = new FriendshipRecord(requester, recipient);
        friendshipMapper.insert(friendship);
        NotificationRecord notification = new NotificationRecord(
                recipient,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                friendship.getId(),
                "{\"message\":\"hello\"}"
        );

        notificationMapper.insert(notification);
        NotificationRecord duplicate = new NotificationRecord(
                recipient,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                friendship.getId(),
                "{\"message\":\"hello\"}"
        );

        assertThatThrownBy(() -> notificationMapper.insert(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(notificationMapper.findByBusinessKey(
                recipient,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                friendship.getId()
        ).getId()).isEqualTo(notification.getId());
    }

    @DisplayName("NotificationMapper는 친구 요청 알림을 reference 기준으로 읽음 처리한다")
    @Test
    void notificationMapperFindsAndMarksFriendRequestNotifications() {
        String requester = uniqueId("noti-requester");
        String recipient = uniqueId("noti-recipient");
        seedMember(requester, requester + "@example.com");
        seedMember(recipient, recipient + "@example.com");
        FriendshipRecord friendship = new FriendshipRecord(requester, recipient);
        friendshipMapper.insert(friendship);
        NotificationRecord notification = new NotificationRecord(
                recipient,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                friendship.getId(),
                "{\"message\":\"hello\"}"
        );

        notificationMapper.insert(notification);
        notificationMapper.markReadByReference(
                recipient,
                NotificationReferenceType.FRIENDSHIP,
                friendship.getId(),
                LocalDateTime.now()
        );
        NotificationRecord saved = notificationMapper.findByBusinessKey(
                recipient,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                friendship.getId()
        );

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
        assertThat(saved.getReadAt()).isNotNull();
    }
}
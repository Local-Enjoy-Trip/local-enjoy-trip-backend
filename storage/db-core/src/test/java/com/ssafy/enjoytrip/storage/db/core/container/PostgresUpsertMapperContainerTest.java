package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.core.domain.NotificationType;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionCountRecord;
import com.ssafy.enjoytrip.storage.db.core.model.FriendshipRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NotificationRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.FriendshipMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NotificationMapper;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PostgresUpsertMapperContainerTest extends StorageContainerTestSupport {
    private static final AtomicLong ATTRACTION_IDS = new AtomicLong(9_800_000L);

    @Autowired
    private AttractionMapper attractionMapper;

    @Autowired
    private FriendshipMapper friendshipMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @DisplayName("NotificationMapper는 PostgreSQL on conflict로 친구 요청 알림을 upsert한다")
    @Test
    void notificationMapperUpsertsFriendRequestNotificationsByBusinessKey() {
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
                requester
        );

        notificationMapper.upsertFriendRequest(notification);
        NotificationRecord inserted = notificationMapper.findByBusinessKey(
                recipient,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                friendship.getId()
        );
        NotificationRecord duplicate = new NotificationRecord(
                recipient,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                friendship.getId(),
                "updated"
        );

        notificationMapper.upsertFriendRequest(duplicate);
        NotificationRecord updated = notificationMapper.findByBusinessKey(
                recipient,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                friendship.getId()
        );

        assertThat(updated.getId()).isEqualTo(inserted.getId());
        assertThat(updated.getPayload()).isEqualTo("updated");
    }

    @DisplayName("AttractionMapper는 PostgreSQL on conflict로 favorite delta를 0 미만으로 내리지 않는다")
    @Test
    void attractionMapperAppliesPopularityFavoriteDeltaWithPostgresUpsert() {
        long attractionId = ATTRACTION_IDS.incrementAndGet();
        seedAttraction(attractionId, "컨테이너 인기 관광지", 1, 1);

        assertThat(attractionMapper.applyPopularityFavoriteDelta(attractionId, 3L)).isEqualTo(1);
        assertThat(attractionMapper.applyPopularityFavoriteDelta(attractionId, -5L)).isEqualTo(1);

        List<AttractionCountRecord> counts = attractionMapper.findPopularityFavoriteCounts(
                List.of(attractionId)
        );

        assertThat(counts).extracting(AttractionCountRecord::count).containsExactly(0);
    }
}

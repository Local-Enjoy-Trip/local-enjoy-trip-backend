package com.ssafy.enjoytrip.core.api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ssafy.enjoytrip.core.domain.event.FriendshipRequestedEvent;
import com.ssafy.enjoytrip.core.domain.event.listener.FriendshipNotificationEventListener;
import com.ssafy.enjoytrip.core.domain.service.FriendshipService;
import com.ssafy.enjoytrip.core.domain.service.NotificationService;
import com.ssafy.enjoytrip.storage.db.core.model.FriendshipRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.FriendshipMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NotificationMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class FriendshipServiceTest {
    @DisplayName("친구 요청은 알림 서비스를 직접 호출하지 않고 이벤트를 발행한다")
    @Test
    void requestFriendshipPublishesFriendshipRequestedEvent() {
        FriendshipMapper friendshipMapper = mock(FriendshipMapper.class);
        MemberMapper memberMapper = mock(MemberMapper.class);
        NotificationMapper notificationMapper = mock(NotificationMapper.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        FriendshipService service = new FriendshipService(
                friendshipMapper,
                memberMapper,
                notificationMapper,
                eventPublisher
        );
        doAnswer(invocation -> {
            FriendshipRecord record = invocation.getArgument(0);
            record.setId(11L);
            return 1;
        }).when(friendshipMapper).insert(any(FriendshipRecord.class));

        service.requestFriendship("alice", "bob");

        verify(eventPublisher).publishEvent(new FriendshipRequestedEvent(11L, "alice", "bob"));
    }

    @DisplayName("친구 요청 이벤트 리스너는 알림 생성을 별도 경계에서 실행한다")
    @Test
    void friendshipRequestedListenerSavesNotification() {
        NotificationService notificationService = mock(NotificationService.class);
        FriendshipNotificationEventListener listener =
                new FriendshipNotificationEventListener(notificationService);

        listener.saveFriendRequestNotification(new FriendshipRequestedEvent(11L, "alice", "bob"));

        verify(notificationService).saveFriendRequestReceived(11L, "alice", "bob");
    }
}

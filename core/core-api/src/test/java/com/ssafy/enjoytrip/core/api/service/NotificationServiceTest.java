package com.ssafy.enjoytrip.core.api.service;

import static com.ssafy.enjoytrip.core.domain.NotificationReferenceType.FRIENDSHIP;
import static com.ssafy.enjoytrip.core.domain.NotificationType.FRIEND_REQUEST_RECEIVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.Notification;
import com.ssafy.enjoytrip.core.domain.service.NotificationService;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NotificationRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseInvitationMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.FriendshipMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NotificationMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NotificationServiceTest {
    @DisplayName("친구 요청 알림 payload는 JSON 문자열이 아니라 요청자 email을 그대로 저장한다")
    @Test
    void saveFriendRequestReceivedStoresRequesterEmailAsPayload() {
        NotificationMapper notificationMapper = mock(NotificationMapper.class);
        FriendshipMapper friendshipMapper = mock(FriendshipMapper.class);
        MemberMapper memberMapper = mock(MemberMapper.class);
        CourseInvitationMapper courseInvitationMapper = mock(CourseInvitationMapper.class);
        NotificationService service = new NotificationService(
                notificationMapper,
                friendshipMapper,
                memberMapper,
                courseInvitationMapper
        );
        MemberRecord requester = new MemberRecord("Alice", "alice", "alice@example.com", "password", null);
        when(memberMapper.findById(11L)).thenReturn(requester);
        NotificationRecord saved = new NotificationRecord(
                22L,
                FRIEND_REQUEST_RECEIVED,
                FRIENDSHIP,
                33L,
                "alice@example.com"
        );
        saved.setId(1L);
        when(notificationMapper.findByBusinessKey(22L, FRIEND_REQUEST_RECEIVED, FRIENDSHIP, 33L))
                .thenReturn(saved);

        Notification notification = service.saveFriendRequestReceived(33L, 11L, 22L);

        ArgumentCaptor<NotificationRecord> recordCaptor = ArgumentCaptor.forClass(NotificationRecord.class);
        verify(notificationMapper).upsertFriendRequest(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getPayload()).isEqualTo("alice@example.com");
        assertThat(notification.payload()).isEqualTo("alice@example.com");
    }
}

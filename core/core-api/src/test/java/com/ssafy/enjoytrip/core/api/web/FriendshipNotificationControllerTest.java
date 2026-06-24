package com.ssafy.enjoytrip.core.api.web;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ssafy.enjoytrip.core.domain.Friendship;
import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.core.domain.Notification;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.core.domain.NotificationType;
import com.ssafy.enjoytrip.core.domain.service.FriendshipService;
import com.ssafy.enjoytrip.core.domain.service.NotificationService;
import com.ssafy.enjoytrip.core.api.web.controller.FriendshipController;
import com.ssafy.enjoytrip.core.api.web.controller.NotificationController;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FriendshipNotificationControllerTest {
    private FriendshipService friendshipService;
    private NotificationService notificationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        friendshipService = mock(FriendshipService.class);
        notificationService = mock(NotificationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new FriendshipController(friendshipService),
                        new NotificationController(notificationService)
                )
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @DisplayName("인증 사용자는 JSON 본문으로 친구 요청을 생성한다")
    @Test
    void requestFriendshipWithJsonBody() throws Exception {
        when(friendshipService.requestFriendship(eq(1L), eq("bob@example.com")))
                .thenReturn(friendship(
                        1L,
                        1L,
                        "alice@example.com",
                        2L,
                        "bob@example.com",
                        FriendshipStatus.PENDING
                ));

        mockMvc.perform(post("/api/friendships/requests")
                        .principal(jwtPrincipal("1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetEmail\":\"bob@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.friendship.id").value(1))
                .andExpect(jsonPath("$.data.friendship.status").value("PENDING"))
                .andExpect(jsonPath("$.error", nullValue()));

        verify(friendshipService).requestFriendship(1L, "bob@example.com");
    }

    @DisplayName("친구 목록은 현재 사용자 관점의 counterpart를 friends 필드로 반환한다")
    @Test
    void friendsReturnsCounterpartList() throws Exception {
        when(friendshipService.findFriends(1L))
                .thenReturn(List.of(friendship(
                        1L,
                        2L,
                        "bob@example.com",
                        1L,
                        "alice@example.com",
                        FriendshipStatus.ACCEPTED
                )));

        mockMvc.perform(get("/api/friendships").principal(jwtPrincipal("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.friends[0].friendshipId").value(1))
                .andExpect(jsonPath("$.data.friends[0].email").value("bob@example.com"));
    }

    @DisplayName("내 알림 조회는 미처리 친구 요청 알림만 notifications 필드로 반환한다")
    @Test
    void notificationsReturnsCurrentRecipientUnreadNotifications() throws Exception {
        when(notificationService.findNotifications(2L, 10))
                .thenReturn(List.of(notification(3L, 2L, null)));

        mockMvc.perform(get("/api/notifications?limit=10")
                        .principal(jwtPrincipal("2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notifications[0].id").value(3))
                .andExpect(jsonPath("$.data.notifications[0].type").value("FRIEND_REQUEST_RECEIVED"))
                .andExpect(jsonPath("$.data.notifications[0].read").value(false));

        verify(notificationService).findNotifications(2L, 10);
    }

    @DisplayName("안 읽은 알림 상태 조회는 존재 여부만 hasUnread 필드로 반환한다")
    @Test
    void unreadStatusReturnsHasUnreadOnly() throws Exception {
        when(notificationService.hasUnreadNotification(2L))
                .thenReturn(true);

        mockMvc.perform(get("/api/notifications/unread-status")
                        .principal(jwtPrincipal("2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasUnread").value(true));

        verify(notificationService).hasUnreadNotification(2L);
    }

    private static Friendship friendship(Long id,
                                         Long requesterMemberId,
                                         String requesterEmail,
                                         Long addresseeMemberId,
                                         String addresseeEmail,
                                         FriendshipStatus status) {
        return new Friendship(
                id,
                requesterMemberId,
                requesterEmail,
                requesterEmail,
                addresseeMemberId,
                addresseeEmail,
                addresseeEmail,
                status,
                LocalDateTime.of(2026, 6, 12, 10, 0),
                null,
                LocalDateTime.of(2026, 6, 12, 10, 0),
                null
        );
    }

    private static Notification notification(Long id, Long recipientMemberId, LocalDateTime readAt) {
        return new Notification(
                id,
                recipientMemberId,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                1L,
                "alice",
                readAt,
                LocalDateTime.of(2026, 6, 12, 10, 0),
                null
        );
    }

    private static Principal jwtPrincipal(String subject) {
        Jwt jwt = Jwt.withTokenValue("token-" + subject)
                .header("alg", "none")
                .subject(subject)
                .issuedAt(Instant.parse("2026-06-12T00:00:00Z"))
                .expiresAt(Instant.parse("2026-06-12T02:00:00Z"))
                .build();
        return new JwtAuthenticationToken(jwt);
    }
}

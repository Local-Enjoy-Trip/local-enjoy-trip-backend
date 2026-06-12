package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.domain.FriendshipStatus.ACCEPTED;
import static com.ssafy.enjoytrip.domain.FriendshipStatus.PENDING;
import static com.ssafy.enjoytrip.support.error.ErrorType.FRIENDSHIP_ACCESS_DENIED;
import static com.ssafy.enjoytrip.support.error.ErrorType.FRIENDSHIP_ALREADY_ACTIVE;
import static com.ssafy.enjoytrip.support.error.ErrorType.FRIENDSHIP_SELF_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ssafy.enjoytrip.domain.Friendship;
import com.ssafy.enjoytrip.domain.FriendshipStatus;
import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.domain.Notification;
import com.ssafy.enjoytrip.domain.NotificationOutboxEvent;
import com.ssafy.enjoytrip.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.repository.FriendshipRepository;
import com.ssafy.enjoytrip.repository.MemberRepository;
import com.ssafy.enjoytrip.repository.NotificationOutboxRepository;
import com.ssafy.enjoytrip.repository.NotificationRepository;
import com.ssafy.enjoytrip.support.error.CoreException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FriendshipServiceTest {

    @DisplayName("친구 요청은 PENDING 관계와 알림 outbox 이벤트를 함께 저장한다")
    @Test
    void requestFriendshipSavesPendingAndOutboxTogether() {
        FakeFriendshipRepository friendships = new FakeFriendshipRepository();
        FakeOutboxRepository outbox = new FakeOutboxRepository();
        FriendshipService service = service(friendships, outbox, "alice", "bob");

        Friendship result = service.requestFriendship("alice", "bob");

        assertEquals(PENDING, result.status());
        assertEquals("alice", result.requesterUserId());
        assertEquals("bob", result.addresseeUserId());
        assertEquals(result.id(), outbox.lastFriendshipId);
        assertEquals("alice", outbox.lastRequesterUserId);
        assertEquals("bob", outbox.lastRecipientUserId);
    }

    @DisplayName("자기 자신에게 친구 요청하면 self request 오류로 거부한다")
    @Test
    void requestFriendshipRejectsSelfRequest() {
        FriendshipService service = service(new FakeFriendshipRepository(), new FakeOutboxRepository(), "alice");

        CoreException exception = assertThrows(CoreException.class,
                () -> service.requestFriendship("alice", "alice"));

        assertEquals(FRIENDSHIP_SELF_REQUEST, exception.errorType());
    }

    @DisplayName("이미 active 관계가 있으면 중복 친구 요청을 conflict로 거부한다")
    @Test
    void requestFriendshipRejectsActiveDuplicate() {
        FakeFriendshipRepository friendships = new FakeFriendshipRepository();
        friendships.activePairs.add(pair("alice", "bob"));
        FriendshipService service = service(friendships, new FakeOutboxRepository(), "alice", "bob");

        CoreException exception = assertThrows(CoreException.class,
                () -> service.requestFriendship("alice", "bob"));

        assertEquals(FRIENDSHIP_ALREADY_ACTIVE, exception.errorType());
    }

    @DisplayName("받은 사람만 대기 친구 요청을 수락할 수 있다")
    @Test
    void acceptRequiresAddressee() {
        FakeFriendshipRepository friendships = new FakeFriendshipRepository();
        friendships.saved.add(friendship(1L, "alice", "bob", PENDING));
        FriendshipService service = service(friendships, new FakeOutboxRepository(), "alice", "bob");

        CoreException exception = assertThrows(CoreException.class,
                () -> service.acceptRequest(1L, "alice"));

        assertEquals(FRIENDSHIP_ACCESS_DENIED, exception.errorType());
    }

    @DisplayName("받은 사람이 대기 친구 요청을 수락하면 ACCEPTED 상태가 되고 알림을 읽음 처리한다")
    @Test
    void acceptTransitionsPendingToAccepted() {
        FakeFriendshipRepository friendships = new FakeFriendshipRepository();
        FakeNotificationRepository notifications = new FakeNotificationRepository();
        friendships.saved.add(friendship(1L, "alice", "bob", PENDING));
        FriendshipService service = service(friendships, new FakeOutboxRepository(), notifications, "alice", "bob");

        Friendship result = service.acceptRequest(1L, "bob");

        assertEquals(ACCEPTED, result.status());
        assertEquals("bob", notifications.lastRecipientUserId);
        assertEquals(NotificationReferenceType.FRIENDSHIP, notifications.lastReferenceType);
        assertEquals(1L, notifications.lastReferenceId);
    }

    @DisplayName("받은 사람이 대기 친구 요청을 거절하면 REJECTED 상태가 되고 알림을 읽음 처리한다")
    @Test
    void rejectTransitionsPendingToRejectedAndMarksNotificationRead() {
        FakeFriendshipRepository friendships = new FakeFriendshipRepository();
        FakeNotificationRepository notifications = new FakeNotificationRepository();
        friendships.saved.add(friendship(1L, "alice", "bob", PENDING));
        FriendshipService service = service(friendships, new FakeOutboxRepository(), notifications, "alice", "bob");

        Friendship result = service.rejectRequest(1L, "bob");

        assertEquals(FriendshipStatus.REJECTED, result.status());
        assertEquals("bob", notifications.lastRecipientUserId);
        assertEquals(NotificationReferenceType.FRIENDSHIP, notifications.lastReferenceType);
        assertEquals(1L, notifications.lastReferenceId);
    }

    private static FriendshipService service(FakeFriendshipRepository friendships,
                                             FakeOutboxRepository outbox,
                                             String... users) {
        return service(friendships, outbox, new FakeNotificationRepository(), users);
    }

    private static FriendshipService service(FakeFriendshipRepository friendships,
                                             FakeOutboxRepository outbox,
                                             FakeNotificationRepository notifications,
                                             String... users) {
        FakeMemberRepository members = new FakeMemberRepository(Set.of(users));
        return new FriendshipService(friendships, outbox, notifications, members);
    }

    private static Friendship friendship(Long id, String requester, String addressee, FriendshipStatus status) {
        return new Friendship(
                id,
                requester,
                requester,
                addressee,
                addressee,
                status,
                LocalDateTime.of(2026, 6, 12, 10, 0),
                null,
                LocalDateTime.of(2026, 6, 12, 10, 0),
                null
        );
    }

    private static String pair(String first, String second) {
        return first.compareTo(second) < 0 ? first + ":" + second : second + ":" + first;
    }

    private static class FakeFriendshipRepository implements FriendshipRepository {
        private final List<Friendship> saved = new ArrayList<>();
        private final Set<String> activePairs = new HashSet<>();
        private long sequence = 1;

        @Override
        public Optional<Friendship> findById(Long id) {
            return saved.stream().filter(friendship -> friendship.id().equals(id)).findFirst();
        }

        @Override
        public boolean existsActiveBetween(String userId, String otherUserId) {
            return activePairs.contains(pair(userId, otherUserId));
        }

        @Override
        public Friendship savePending(String requesterUserId, String addresseeUserId) {
            Friendship friendship = friendship(sequence++, requesterUserId, addresseeUserId, PENDING);
            saved.add(friendship);
            activePairs.add(pair(requesterUserId, addresseeUserId));
            return friendship;
        }

        @Override
        public Friendship updateStatus(Long id, FriendshipStatus status) {
            Friendship existing = findById(id).orElseThrow();
            Friendship updated = friendship(id, existing.requesterUserId(), existing.addresseeUserId(), status);
            saved.remove(existing);
            saved.add(updated);
            return updated;
        }

        @Override
        public List<Friendship> findAcceptedByUser(String userId) {
            return List.of();
        }

        @Override
        public List<Friendship> findPendingReceivedByUser(String userId) {
            return List.of();
        }

        @Override
        public List<Friendship> findPendingSentByUser(String userId) {
            return List.of();
        }
    }

    private static class FakeNotificationRepository implements NotificationRepository {
        private String lastRecipientUserId;
        private NotificationReferenceType lastReferenceType;
        private Long lastReferenceId;

        @Override
        public boolean existsByOutboxEventId(Long outboxEventId) {
            return false;
        }

        @Override
        public boolean existsUnreadByRecipient(String recipientUserId) {
            return false;
        }

        @Override
        public Notification saveFromOutbox(NotificationOutboxEvent event) {
            return null;
        }

        @Override
        public List<Notification> findUnreadByRecipient(String recipientUserId, int limit) {
            return List.of();
        }

        @Override
        public int markReadByReference(String recipientUserId, NotificationReferenceType referenceType, Long referenceId) {
            this.lastRecipientUserId = recipientUserId;
            this.lastReferenceType = referenceType;
            this.lastReferenceId = referenceId;
            return 1;
        }
    }

    private static class FakeOutboxRepository implements NotificationOutboxRepository {
        private Long lastFriendshipId;
        private String lastRequesterUserId;
        private String lastRecipientUserId;

        @Override
        public NotificationOutboxEvent saveFriendRequestReceived(Long friendshipId,
                                                                 String requesterUserId,
                                                                 String requesterDisplayName,
                                                                 String recipientUserId) {
            this.lastFriendshipId = friendshipId;
            this.lastRequesterUserId = requesterUserId;
            this.lastRecipientUserId = recipientUserId;
            return null;
        }

        @Override
        public Optional<NotificationOutboxEvent> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public void markProcessed(Long id) {
        }

        @Override
        public void markFailed(Long id, String lastError) {
        }
    }

    private static class FakeMemberRepository implements MemberRepository {
        private final Set<String> users;

        FakeMemberRepository(Set<String> users) {
            this.users = users;
        }

        @Override
        public List<Member> findAll() {
            return List.of();
        }

        @Override
        public Member findByUserId(String userId) {
            return new Member(userId, userId, userId, userId + "@example.com", "password", null,
                    null, null, null, "2026-06-12T10:00:00");
        }

        @Override
        public Member findByEmail(String email) {
            return null;
        }

        @Override
        public String findPassword(String userId, String email) {
            return null;
        }

        @Override
        public boolean existsByUserId(String userId) {
            return users.contains(userId);
        }

        @Override
        public boolean existsByEmail(String email) {
            return false;
        }

        @Override
        public void insert(Member member) {
        }

        @Override
        public boolean update(Member member) {
            return false;
        }

        @Override
        public boolean delete(String userId) {
            return false;
        }

        @Override
        public void insertAuthLog(String userId, String eventType) {
        }
    }
}

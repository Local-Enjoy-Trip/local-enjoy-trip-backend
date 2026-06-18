package com.ssafy.enjoytrip.storage.db.core.jpa;

import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.core.domain.NotificationType;
import com.ssafy.enjoytrip.storage.db.core.entity.NotificationEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {
    boolean existsByOutboxEventId(Long outboxEventId);

    Optional<NotificationEntity> findByOutboxEventId(Long outboxEventId);

    @Query("""
            select count(n) > 0
            from NotificationEntity n
            where n.recipientUserId = :recipientUserId
              and n.readAt is null
              and n.type = :type
              and n.referenceType = :referenceType
              and exists (
                  select f.id
                  from FriendshipEntity f
                  where f.id = n.referenceId
                    and f.status = :status
              )
            """)
    boolean existsUnreadFriendRequest(@Param("recipientUserId") String recipientUserId,
                                      @Param("type") NotificationType type,
                                      @Param("referenceType") NotificationReferenceType referenceType,
                                      @Param("status") FriendshipStatus status);

    @Query("""
            select n
            from NotificationEntity n
            where n.recipientUserId = :recipientUserId
              and n.readAt is null
              and n.type = :type
              and n.referenceType = :referenceType
              and exists (
                  select f.id
                  from FriendshipEntity f
                  where f.id = n.referenceId
                    and f.status = :status
              )
            order by n.createdAt desc, n.id desc
            """)
    List<NotificationEntity> findUnreadFriendRequests(@Param("recipientUserId") String recipientUserId,
                                                       @Param("type") NotificationType type,
                                                       @Param("referenceType") NotificationReferenceType referenceType,
                                                       @Param("status") FriendshipStatus status,
                                                       Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update NotificationEntity n
            set n.readAt = :readAt
            where n.recipientUserId = :recipientUserId
              and n.referenceType = :referenceType
              and n.referenceId = :referenceId
              and n.readAt is null
            """)
    int markReadByReference(@Param("recipientUserId") String recipientUserId,
                            @Param("referenceType") NotificationReferenceType referenceType,
                            @Param("referenceId") Long referenceId,
                            @Param("readAt") LocalDateTime readAt);
}

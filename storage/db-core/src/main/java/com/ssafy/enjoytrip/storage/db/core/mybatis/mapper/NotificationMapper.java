package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.core.domain.NotificationType;
import com.ssafy.enjoytrip.storage.db.core.entity.NotificationEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NotificationMapper {
    int existsByOutboxEventId(Long outboxEventId);

    NotificationEntity findByOutboxEventId(Long outboxEventId);

    int existsUnreadFriendRequest(@Param("recipientUserId") String recipientUserId,
                                  @Param("type") NotificationType type,
                                  @Param("referenceType") NotificationReferenceType referenceType,
                                  @Param("status") FriendshipStatus status);

    List<NotificationEntity> findUnreadFriendRequests(@Param("recipientUserId") String recipientUserId,
                                                       @Param("type") NotificationType type,
                                                       @Param("referenceType") NotificationReferenceType referenceType,
                                                       @Param("status") FriendshipStatus status,
                                                       @Param("limit") int limit);

    int insert(NotificationEntity entity);

    int updateReadAt(NotificationEntity entity);

    int markReadByReference(@Param("recipientUserId") String recipientUserId,
                            @Param("referenceType") NotificationReferenceType referenceType,
                            @Param("referenceId") Long referenceId,
                            @Param("readAt") LocalDateTime readAt);
}

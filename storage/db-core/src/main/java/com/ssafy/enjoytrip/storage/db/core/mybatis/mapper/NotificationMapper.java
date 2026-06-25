package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.core.domain.CourseInvitationStatus;
import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.core.domain.NotificationType;
import com.ssafy.enjoytrip.storage.db.core.model.NotificationRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NotificationMapper {
    NotificationRecord findByBusinessKey(@Param("recipientMemberId") Long recipientMemberId,
                                          @Param("type") NotificationType type,
                                          @Param("referenceType") NotificationReferenceType referenceType,
                                          @Param("referenceId") Long referenceId);

    int existsUnreadFriendRequest(@Param("recipientMemberId") Long recipientMemberId,
                                  @Param("type") NotificationType type,
                                  @Param("referenceType") NotificationReferenceType referenceType,
                                  @Param("status") FriendshipStatus status);

    List<NotificationRecord> findUnreadFriendRequests(
            @Param("recipientMemberId") Long recipientMemberId,
            @Param("type") NotificationType type,
            @Param("referenceType") NotificationReferenceType referenceType,
            @Param("status") FriendshipStatus status,
            @Param("limit") int limit
    );

    int upsertFriendRequest(NotificationRecord record);

    int existsUnreadCourseInvitation(
            @Param("recipientMemberId") Long recipientMemberId,
            @Param("type") NotificationType type,
            @Param("referenceType") NotificationReferenceType referenceType,
            @Param("status") CourseInvitationStatus status
    );

    List<NotificationRecord> findUnreadCourseInvitations(
            @Param("recipientMemberId") Long recipientMemberId,
            @Param("type") NotificationType type,
            @Param("referenceType") NotificationReferenceType referenceType,
            @Param("status") CourseInvitationStatus status,
            @Param("limit") int limit
    );

    int upsertCourseInvitation(NotificationRecord record);

    int updateReadAt(NotificationRecord record);

    int markReadByReference(@Param("recipientMemberId") Long recipientMemberId,
                            @Param("referenceType") NotificationReferenceType referenceType,
                            @Param("referenceId") Long referenceId,
                            @Param("readAt") LocalDateTime readAt);
}

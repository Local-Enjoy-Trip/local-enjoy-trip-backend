package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.domain.NotificationReferenceType.COURSE_INVITATION;
import static com.ssafy.enjoytrip.core.domain.NotificationReferenceType.FRIENDSHIP;
import static com.ssafy.enjoytrip.core.domain.NotificationType.COURSE_INVITATION_RECEIVED;
import static com.ssafy.enjoytrip.core.domain.NotificationType.FRIEND_REQUEST_RECEIVED;

import com.ssafy.enjoytrip.core.domain.CourseInvitationStatus;
import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.core.domain.Notification;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.storage.db.core.model.CourseInvitationRecord;
import com.ssafy.enjoytrip.storage.db.core.model.FriendshipRecord;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NotificationRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseInvitationMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.FriendshipMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NotificationMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;
    private final FriendshipMapper friendshipMapper;
    private final MemberMapper memberMapper;
    private final CourseInvitationMapper courseInvitationMapper;

    public List<Notification> findNotifications(Long recipientMemberId, int limit) {
        List<Notification> result = new ArrayList<>();
        result.addAll(findUnreadFriendRequestsByRecipient(recipientMemberId, limit));
        result.addAll(findUnreadCourseInvitationsByRecipient(recipientMemberId, limit));
        return result;
    }

    public boolean hasUnreadNotification(Long recipientMemberId) {
        boolean hasUnreadFriendRequest = notificationMapper.existsUnreadFriendRequest(
                recipientMemberId,
                FRIEND_REQUEST_RECEIVED,
                FRIENDSHIP,
                FriendshipStatus.PENDING
        ) > 0;

        if (hasUnreadFriendRequest) {
            return true;
        }

        return notificationMapper.existsUnreadCourseInvitation(
                recipientMemberId,
                COURSE_INVITATION_RECEIVED,
                COURSE_INVITATION,
                CourseInvitationStatus.PENDING
        ) > 0;
    }

    public Notification saveFriendRequestReceived(Long friendshipId,
                                                  Long requesterMemberId,
                                                  Long recipientMemberId) {
        NotificationRecord record = new NotificationRecord(
                recipientMemberId,
                FRIEND_REQUEST_RECEIVED,
                FRIENDSHIP,
                friendshipId,
                requesterEmail(requesterMemberId)
        );
        markReadIfFriendRequestAlreadyHandled(record);

        notificationMapper.upsertFriendRequest(record);
        return toNotification(notificationMapper.findByBusinessKey(
                recipientMemberId,
                FRIEND_REQUEST_RECEIVED,
                FRIENDSHIP,
                friendshipId
        ));
    }

    public Notification saveCourseInvitationNotification(Long invitationId,
                                                         String courseTitle,
                                                         Long inviterMemberId,
                                                         Long inviteeMemberId) {
        String payload = inviterEmail(inviterMemberId) + "|" + courseTitle;
        NotificationRecord record = new NotificationRecord(
                inviteeMemberId,
                COURSE_INVITATION_RECEIVED,
                COURSE_INVITATION,
                invitationId,
                payload
        );
        markReadIfCourseInvitationAlreadyHandled(record);

        notificationMapper.upsertCourseInvitation(record);
        return toNotification(notificationMapper.findByBusinessKey(
                inviteeMemberId,
                COURSE_INVITATION_RECEIVED,
                COURSE_INVITATION,
                invitationId
        ));
    }

    public int markReadByReference(Long recipientMemberId,
                                   NotificationReferenceType referenceType,
                                   Long referenceId) {
        return notificationMapper.markReadByReference(
                recipientMemberId,
                referenceType,
                referenceId,
                LocalDateTime.now()
        );
    }

    private List<Notification> findUnreadFriendRequestsByRecipient(Long recipientMemberId, int limit) {
        return notificationMapper.findUnreadFriendRequests(
                        recipientMemberId,
                        FRIEND_REQUEST_RECEIVED,
                        FRIENDSHIP,
                        FriendshipStatus.PENDING,
                        limit
                ).stream()
                .map(this::toNotification)
                .toList();
    }

    private List<Notification> findUnreadCourseInvitationsByRecipient(Long recipientMemberId, int limit) {
        return notificationMapper.findUnreadCourseInvitations(
                        recipientMemberId,
                        COURSE_INVITATION_RECEIVED,
                        COURSE_INVITATION,
                        CourseInvitationStatus.PENDING,
                        limit
                ).stream()
                .map(this::toNotification)
                .toList();
    }

    private void markReadIfFriendRequestAlreadyHandled(NotificationRecord record) {
        FriendshipRecord friendship = friendshipMapper.findById(record.getReferenceId());
        if (friendship != null && friendship.getStatus() != FriendshipStatus.PENDING) {
            record.markRead();
        }
    }

    private void markReadIfCourseInvitationAlreadyHandled(NotificationRecord record) {
        CourseInvitationRecord invitation = courseInvitationMapper.findById(record.getReferenceId());
        if (invitation != null && invitation.getStatus() != CourseInvitationStatus.PENDING) {
            record.markRead();
        }
    }

    private Notification toNotification(NotificationRecord record) {
        return new Notification(
                record.getId(),
                record.getRecipientMemberId(),
                record.getType(),
                record.getReferenceType(),
                record.getReferenceId(),
                record.getPayload(),
                record.getReadAt(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private String requesterEmail(Long requesterMemberId) {
        MemberRecord requester = memberMapper.findById(requesterMemberId);
        if (requester == null) {
            return "";
        }
        return requester.getEmail();
    }

    private String inviterEmail(Long inviterMemberId) {
        MemberRecord inviter = memberMapper.findById(inviterMemberId);
        if (inviter == null) {
            return "";
        }
        return inviter.getEmail();
    }
}

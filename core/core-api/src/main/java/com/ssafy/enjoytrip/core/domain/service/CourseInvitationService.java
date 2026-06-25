package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.domain.CourseInvitationStatus.ACCEPTED;
import static com.ssafy.enjoytrip.core.domain.CourseInvitationStatus.PENDING;
import static com.ssafy.enjoytrip.core.domain.CourseInvitationStatus.REJECTED;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_ACCESS_DENIED;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVITATION_ACCESS_DENIED;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVITATION_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVITATION_NOT_FOUND;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_NOT_FOUND;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.NOT_FRIENDS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.CourseInvitation;
import com.ssafy.enjoytrip.core.domain.CourseInvitationStatus;
import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.core.domain.event.CourseInvitationSentEvent;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.CourseInvitationRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseInvitationMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.FriendshipMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseInvitationService {
    private final CourseInvitationMapper courseInvitationMapper;
    private final CourseMapper courseMapper;
    private final MemberMapper memberMapper;
    private final FriendshipMapper friendshipMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CourseInvitation inviteFriend(Long hostMemberId, String courseId, String inviteeEmail) {
        CourseRecord course = findRequiredCourse(courseId);
        validateHost(course, hostMemberId);

        MemberRecord invitee = findRequiredMemberByEmail(inviteeEmail);
        validateFriendship(hostMemberId, invitee.getId());
        validateNotAlreadyInvited(courseId, invitee.getId());

        CourseInvitationRecord record = new CourseInvitationRecord(courseId, hostMemberId, invitee.getId());
        courseInvitationMapper.insert(record);

        CourseInvitationRecord saved = courseInvitationMapper.findById(record.getId());
        publishInvitationSent(saved, course.getTitle());
        return toInvitation(saved);
    }

    @Transactional
    public CourseInvitation acceptInvitation(Long inviteeMemberId, Long invitationId) {
        CourseInvitationRecord record = findRequiredPendingInvitation(invitationId, inviteeMemberId);
        courseInvitationMapper.updateStatus(record.getId(), ACCEPTED, LocalDateTime.now());
        courseMapper.insertSave(record.getCourseId(), inviteeMemberId);
        return toInvitation(courseInvitationMapper.findById(record.getId()));
    }

    @Transactional
    public CourseInvitation rejectInvitation(Long inviteeMemberId, Long invitationId) {
        CourseInvitationRecord record = findRequiredPendingInvitation(invitationId, inviteeMemberId);
        courseInvitationMapper.updateStatus(record.getId(), REJECTED, LocalDateTime.now());
        return toInvitation(courseInvitationMapper.findById(record.getId()));
    }

    public List<CourseInvitation> findByCourse(Long hostMemberId, String courseId) {
        CourseRecord course = findRequiredCourse(courseId);
        validateHost(course, hostMemberId);

        return courseInvitationMapper.findByCourseId(courseId).stream()
                .map(this::toInvitation)
                .toList();
    }

    private CourseInvitationRecord findRequiredPendingInvitation(Long invitationId, Long inviteeMemberId) {
        CourseInvitationRecord record = courseInvitationMapper.findById(invitationId);
        if (record == null) {
            throw new CoreException(COURSE_INVITATION_NOT_FOUND);
        }
        if (!record.getInviteeMemberId().equals(inviteeMemberId)) {
            throw new CoreException(COURSE_INVITATION_ACCESS_DENIED);
        }
        if (record.getStatus() != PENDING) {
            throw new CoreException(COURSE_INVITATION_ACCESS_DENIED);
        }
        return record;
    }

    private void validateHost(CourseRecord course, Long memberId) {
        if (course.getDeletedAt() != null) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
        if (!course.getOwnerMemberId().equals(memberId)) {
            throw new CoreException(COURSE_ACCESS_DENIED);
        }
    }

    private void validateFriendship(Long hostMemberId, Long inviteeMemberId) {
        boolean areFriends = friendshipMapper.existsActiveBetween(
                hostMemberId,
                inviteeMemberId,
                List.of(FriendshipStatus.ACCEPTED)
        ) > 0;


        if (!areFriends) {
            throw new CoreException(NOT_FRIENDS);
        }
    }

    private void validateNotAlreadyInvited(String courseId, Long inviteeMemberId) {
        if (courseInvitationMapper.existsByCourseAndInvitee(courseId, inviteeMemberId) > 0) {
            throw new CoreException(COURSE_INVITATION_ALREADY_EXISTS);
        }
    }

    private void publishInvitationSent(CourseInvitationRecord record, String courseTitle) {
        eventPublisher.publishEvent(new CourseInvitationSentEvent(
                record.getId(),
                record.getCourseId(),
                courseTitle,
                record.getInviterMemberId(),
                record.getInviteeMemberId()
        ));
    }

    private CourseRecord findRequiredCourse(String courseId) {
        CourseRecord course = courseMapper.findById(courseId);
        if (course == null) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
        return course;
    }

    private MemberRecord findRequiredMemberByEmail(String email) {
        MemberRecord member = memberMapper.findByEmail(email);
        if (member == null) {
            throw new CoreException(USER_NOT_FOUND);
        }
        return member;
    }

    private CourseInvitation toInvitation(CourseInvitationRecord record) {
        return new CourseInvitation(
                record.getId(),
                record.getCourseId(),
                record.getInviterMemberId(),
                record.getInviteeMemberId(),
                record.getStatus(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}

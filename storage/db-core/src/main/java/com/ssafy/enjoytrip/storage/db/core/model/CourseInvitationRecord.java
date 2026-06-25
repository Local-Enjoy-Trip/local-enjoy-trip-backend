package com.ssafy.enjoytrip.storage.db.core.model;

import com.ssafy.enjoytrip.core.domain.CourseInvitationStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseInvitationRecord extends BaseRecord {

    private Long id;

    private String courseId;

    private Long inviterMemberId;

    private Long inviteeMemberId;

    private CourseInvitationStatus status;

    public CourseInvitationRecord(String courseId,
                                  Long inviterMemberId,
                                  Long inviteeMemberId) {
        this.courseId = courseId;
        this.inviterMemberId = inviterMemberId;
        this.inviteeMemberId = inviteeMemberId;
        this.status = CourseInvitationStatus.PENDING;
    }
}

package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.core.domain.CourseInvitationStatus;
import com.ssafy.enjoytrip.storage.db.core.model.CourseInvitationRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CourseInvitationMapper {

    int insert(CourseInvitationRecord record);

    CourseInvitationRecord findById(@Param("id") Long id);

    List<CourseInvitationRecord> findByCourseId(@Param("courseId") String courseId);

    int updateStatus(@Param("id") Long id,
                     @Param("status") CourseInvitationStatus status,
                     @Param("updatedAt") LocalDateTime updatedAt);

    int existsByCourseAndInvitee(@Param("courseId") String courseId,
                                  @Param("inviteeMemberId") Long inviteeMemberId);

    List<String> findAcceptedCourseIdsByInvitee(@Param("inviteeMemberId") Long inviteeMemberId);
}

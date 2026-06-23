package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_ACCESS_DENIED;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseReader;
import com.ssafy.enjoytrip.core.domain.CourseWriter;
import com.ssafy.enjoytrip.core.domain.MemberRole;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCourseService {
    private final CourseReader courseReader;
    private final MemberMapper memberMapper;
    private final CourseWriter courseWriter;

    public List<Course> findAdminCourses() {
        return courseReader.findAdminCourses();
    }

    public Course createAdminCourse(Course course) {
        requireAdmin(course.ownerMemberId());
        return courseWriter.create(course);
    }

    public Course updateAdminCourse(Long adminMemberId, Course course) {
        requireAdmin(adminMemberId);
        Course current = findRequiredAdminCourse(adminMemberId, course.id());
        current.requireOwnedBy(adminMemberId);

        return courseWriter.update(course);
    }

    public void deleteAdminCourse(Long adminMemberId, String courseId) {
        requireAdmin(adminMemberId);
        Course current = findRequiredAdminCourse(adminMemberId, courseId);
        current.requireOwnedBy(adminMemberId);
        courseWriter.deleteOwned(courseId, adminMemberId);
    }

    private Course findRequiredAdminCourse(Long adminMemberId, String courseId) {
        return courseReader.findRequiredOwned(adminMemberId, courseId);
    }

    private void requireAdmin(Long memberId) {
        MemberRecord member = memberMapper.findById(memberId);
        if (member == null || !MemberRole.ADMIN.name().equals(member.getRole())) {
            throw new CoreException(COURSE_ACCESS_DENIED);
        }
    }
}

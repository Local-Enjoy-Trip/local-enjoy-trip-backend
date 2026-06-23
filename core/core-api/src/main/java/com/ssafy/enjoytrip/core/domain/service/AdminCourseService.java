package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_ACCESS_DENIED;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseReader;
import com.ssafy.enjoytrip.core.domain.CourseWriter;
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
        requireAdmin(course.ownerUserId());
        return courseWriter.create(course);
    }

    public Course updateAdminCourse(String adminUserId, Course course) {
        requireAdmin(adminUserId);
        Course current = findRequiredAdminCourse(adminUserId, course.id());
        current.requireOwnedBy(adminUserId);

        return courseWriter.update(course);
    }

    public void deleteAdminCourse(String adminUserId, String courseId) {
        requireAdmin(adminUserId);
        Course current = findRequiredAdminCourse(adminUserId, courseId);
        current.requireOwnedBy(adminUserId);
        courseWriter.deleteOwned(courseId, adminUserId);
    }

    private Course findRequiredAdminCourse(String adminUserId, String courseId) {
        return courseReader.findRequiredOwned(adminUserId, courseId);
    }

    private void requireAdmin(String userId) {
        MemberRecord member = memberMapper.findByUserId(userId);
        if (member == null || !"ADMIN".equals(member.getRole())) {
            throw new CoreException(COURSE_ACCESS_DENIED);
        }
    }
}

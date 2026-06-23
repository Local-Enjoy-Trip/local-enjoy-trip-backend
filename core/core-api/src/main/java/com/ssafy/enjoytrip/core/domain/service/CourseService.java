package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseFeedSection;
import com.ssafy.enjoytrip.core.domain.CourseOrderOptimizer;
import com.ssafy.enjoytrip.core.domain.CourseReader;
import com.ssafy.enjoytrip.core.domain.CourseWriter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseReader courseReader;
    private final CourseWriter courseWriter;
    private final CourseOrderOptimizer courseOrderOptimizer;

    public List<Course> findMyCourses(String ownerUserId) {
        return courseReader.findMyCourses(ownerUserId);
    }

    public Course findRequired(String id) {
        return courseReader.findRequired(id);
    }

    public Course findPublicRequired(String id) {
        return courseReader.findPublicRequired(id);
    }

    public Course createCourse(Course course) {
        return courseWriter.create(course);
    }

    public Course updateCourse(String ownerUserId, Course course) {
        Course current = findRequired(course.id());
        current.requireOwnedBy(ownerUserId);

        return courseWriter.update(course);
    }

    public Course recommendCourseOrder(String ownerUserId, String courseId) {
        Course current = findRequired(courseId);
        current.requireOwnedBy(ownerUserId);
        return courseOrderOptimizer.recommend(current);
    }

    public void deleteCourse(String ownerUserId, String courseId) {
        Course current = findRequired(courseId);
        current.requireOwnedBy(ownerUserId);
        courseWriter.deleteOwned(courseId, ownerUserId);
    }

    public List<CourseFeedSection> findPublicFeed() {
        return courseReader.findPublicFeed();
    }
}

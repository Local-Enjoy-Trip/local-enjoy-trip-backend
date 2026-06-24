package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseOrderOptimizer;
import com.ssafy.enjoytrip.core.domain.CourseOrderOptimizationContext;
import com.ssafy.enjoytrip.core.domain.CourseReader;
import com.ssafy.enjoytrip.core.domain.CourseWriter;
import com.ssafy.enjoytrip.core.domain.query.DistanceSearchCondition;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseReader courseReader;
    private final CourseWriter courseWriter;
    private final CourseOrderOptimizer courseOrderOptimizer;

    public List<Course> findMyCourses(Long ownerMemberId) {
        return courseReader.findMyCourses(ownerMemberId);
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

    public Course updateCourse(Long ownerMemberId, Course course) {
        Course current = findRequired(course.id());
        current.requireOwnedBy(ownerMemberId);

        return courseWriter.update(course);
    }

    public Course recommendCourseOrder(Long ownerMemberId, String courseId) {
        return recommendCourseOrder(ownerMemberId, courseId, CourseOrderOptimizationContext.empty());
    }

    public Course recommendCourseOrder(Long ownerMemberId,
                                       String courseId,
                                       CourseOrderOptimizationContext context) {
        Course current = findRequired(courseId);
        current.requireOwnedBy(ownerMemberId);
        return courseOrderOptimizer.recommend(current, context);
    }

    public void deleteCourse(Long ownerMemberId, String courseId) {
        Course current = findRequired(courseId);
        current.requireOwnedBy(ownerMemberId);
        courseWriter.deleteOwned(courseId, ownerMemberId);
    }

    public List<Course> findPublicFeed(DistanceSearchCondition condition) {
        return courseReader.findPublicFeed(condition);
    }

    public List<Course> findMdFeed(double longitude, double latitude, int limit) {
        return courseReader.findMdFeed(longitude, latitude, limit);
    }

    public List<Course> findPopularByRegion(String regionName, int limit) {
        return courseReader.findPopularByRegion(regionName, limit);
    }

    public void saveCourse(Long memberId, String courseId) {
        courseWriter.save(courseId, memberId);
    }

    public void unsaveCourse(Long memberId, String courseId) {
        courseWriter.unsave(courseId, memberId);
    }
}

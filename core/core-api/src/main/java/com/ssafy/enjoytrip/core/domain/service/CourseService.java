package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseOrderOptimizer;
import com.ssafy.enjoytrip.core.domain.CourseOrderOptimizationContext;
import com.ssafy.enjoytrip.core.domain.CourseRecommendationCandidate;
import com.ssafy.enjoytrip.core.domain.CourseRecommendationRanker;
import com.ssafy.enjoytrip.core.domain.CourseReader;
import com.ssafy.enjoytrip.core.domain.CourseWriter;
import com.ssafy.enjoytrip.core.domain.NoteTagReader;
import com.ssafy.enjoytrip.core.domain.RerankingContext;
import com.ssafy.enjoytrip.core.domain.event.CourseViewedEvent;
import com.ssafy.enjoytrip.core.domain.query.DistanceSearchCondition;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseReader courseReader;
    private final CourseWriter courseWriter;
    private final CourseOrderOptimizer courseOrderOptimizer;
    private final ApplicationEventPublisher eventPublisher;
    private final CourseRecommendationRanker ranker;
    private final NoteTagReader noteTagReader;

    public List<Course> findMyCourses(Long ownerMemberId) {
        return courseReader.findMyCourses(ownerMemberId);
    }

    public Course findRequired(String id) {
        return courseReader.findRequired(id);
    }

    public Course findPublicRequired(String id) {
        return courseReader.findPublicRequired(id);
    }

    public Course view(String id, Long memberId) {
        Course course = courseReader.findPublicRequired(id);
        if (memberId != null) {
            eventPublisher.publishEvent(new CourseViewedEvent(course.id(), memberId));
        }
        return course;
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

    public List<Course> findPopularByRegion(String regionName, int limit) {
        return courseReader.findPopularByRegion(regionName, limit);
    }

    public List<Course> findAllBySaveCount(int limit) {
        return courseReader.findAllBySaveCount(limit);
    }

    public List<Course> findRecommendations(Long memberId, String regionName, int limit) {
        if (!courseReader.hasMemberProfileEmbedding(memberId)) {
            return courseReader.findPopularByRegion(regionName, limit);
        }

        List<CourseRecommendationCandidate> candidates =
                courseReader.findCandidatesByMemberProfile(memberId, limit * 3);
        RerankingContext context = buildRerankingContext(memberId);

        return ranker.rerank(candidates, context, limit);
    }

    private RerankingContext buildRerankingContext(Long memberId) {
        Set<String> viewedWithin7Days = new HashSet<>(
                courseReader.findRecentlyViewedCourseIds(memberId, 7)
        );
        Set<String> viewedWithin30Days = new HashSet<>(
                courseReader.findRecentlyViewedCourseIds(memberId, 30)
        );
        Map<Long, Long> tagFrequency = noteTagReader.findMemberTagFrequency(memberId);
        return new RerankingContext(viewedWithin7Days, viewedWithin30Days, tagFrequency);
    }

    public void saveCourse(Long memberId, String courseId) {
        courseWriter.save(courseId, memberId);
    }

    public void unsaveCourse(Long memberId, String courseId) {
        courseWriter.unsave(courseId, memberId);
    }
}

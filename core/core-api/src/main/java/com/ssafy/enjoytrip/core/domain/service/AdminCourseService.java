package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_ACCESS_DENIED;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVALID_ITEM;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseRoute;
import com.ssafy.enjoytrip.core.domain.CourseRouteSegment;
import com.ssafy.enjoytrip.core.domain.CourseStop;
import com.ssafy.enjoytrip.core.domain.CourseStopPoint;
import com.ssafy.enjoytrip.core.domain.CourseStopTarget;
import com.ssafy.enjoytrip.core.domain.CourseStopTargetType;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemDetailRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRouteSegmentRecord;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCourseService {
    private static final String TYPE_ATTRACTION = CourseStopTargetType.ATTRACTION.name();
    private static final String TYPE_NOTE = CourseStopTargetType.NOTE.name();

    private final CourseMapper courseMapper;
    private final MemberMapper memberMapper;
    private final CourseStopPointResolver courseStopPointResolver;
    private final CourseRoutePlanner courseRoutePlanner;
    private final CourseWriter courseWriter;

    public List<Course> findAdminCourses() {
        return courseMapper.findAdminOwned().stream()
                .map(record -> toCourse(
                        record,
                        courseMapper.findItemsByCourseId(record.getId()),
                        courseMapper.findSegmentsByCourseId(record.getId())
                ))
                .toList();
    }

    public Course createAdminCourse(Course course) {
        requireAdmin(course.ownerUserId());
        CourseRoute plannedRoute = planRoute(course.route());
        courseWriter.create(course, plannedRoute);
        return findRequiredAdminCourse(course.ownerUserId(), course.id());
    }

    public Course updateAdminCourse(String adminUserId, Course course) {
        requireAdmin(adminUserId);
        Course current = findRequiredAdminCourse(adminUserId, course.id());
        current.requireOwnedBy(adminUserId);

        CourseRoute plannedRoute = planRoute(course.route());
        courseWriter.update(course, plannedRoute);
        return findRequiredAdminCourse(adminUserId, course.id());
    }

    public void deleteAdminCourse(String adminUserId, String courseId) {
        requireAdmin(adminUserId);
        Course current = findRequiredAdminCourse(adminUserId, courseId);
        current.requireOwnedBy(adminUserId);
        courseWriter.deleteOwned(courseId, adminUserId);
    }

    private Course findRequiredAdminCourse(String adminUserId, String courseId) {
        CourseRecord record = courseMapper.findById(courseId);
        if (record == null || record.getDeletedAt() != null) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
        if (!adminUserId.equals(record.getOwnerUserId())) {
            throw new CoreException(COURSE_ACCESS_DENIED);
        }
        return toCourse(
                record,
                courseMapper.findItemsByCourseId(courseId),
                courseMapper.findSegmentsByCourseId(courseId)
        );
    }

    private void requireAdmin(String userId) {
        MemberRecord member = memberMapper.findByUserId(userId);
        if (member == null || !"ADMIN".equals(member.getRole())) {
            throw new CoreException(COURSE_ACCESS_DENIED);
        }
    }

    private CourseRoute planRoute(CourseRoute route) {
        List<CourseStopPoint> points = courseStopPointResolver.resolveAll(route.stops());
        CourseRoute plannedRoute = courseRoutePlanner.plan(points);
        requirePlannedSegments(plannedRoute);
        return plannedRoute;
    }


    private static Course toCourse(CourseRecord record,
                                   List<CourseItemDetailRecord> items,
                                   List<CourseRouteSegmentRecord> segments) {
        return new Course(
                record.getId(),
                record.getOwnerUserId(),
                record.getTitle(),
                record.getRegionName(),
                record.getVisibility(),
                record.getStatus(),
                record.getDescription(),
                record.getCoverImageUrl(),
                record.getCurationSection(),
                record.getCurationOrder(),
                countValue(record.getSaveCount()),
                stringValue(record.getCreatedAt()),
                stringValue(record.getUpdatedAt()),
                toRoute(items, segments)
        );
    }

    private static CourseRoute toRoute(List<CourseItemDetailRecord> items,
                                       List<CourseRouteSegmentRecord> segments) {
        List<CourseStop> stops = items.stream()
                .map(AdminCourseService::toStop)
                .toList();
        Map<Long, Integer> positionsByItemId = positionsByItemId(stops);
        List<CourseRouteSegment> routeSegments = segments.stream()
                .filter(segment -> positionsByItemId.containsKey(segment.getFromCourseItemId()))
                .filter(segment -> positionsByItemId.containsKey(segment.getToCourseItemId()))
                .map(segment -> toSegment(segment, positionsByItemId))
                .toList();
        if (!hasCompleteSegmentSet(stops, routeSegments)) {
            throw new CoreException(COURSE_INVALID_ITEM);
        }
        return CourseRoute.planned(stops, routeSegments);
    }

    private static CourseStop toStop(CourseItemDetailRecord record) {
        return new CourseStop(
                record.id(),
                target(record),
                countValue(record.position()),
                countValue(record.day()),
                record.memo(),
                record.stayMinutes(),
                record.itemTitle()
        );
    }

    private static CourseRouteSegment toSegment(CourseRouteSegmentRecord record,
                                                Map<Long, Integer> positionsByItemId) {
        return new CourseRouteSegment(
                countValue(record.getSegmentOrder()),
                positionsByItemId.get(record.getFromCourseItemId()),
                positionsByItemId.get(record.getToCourseItemId()),
                record.getTravelMode(),
                countValue(record.getDurationSeconds()),
                countValue(record.getDistanceMeters())
        );
    }

    private static Map<Long, Integer> positionsByItemId(List<CourseStop> stops) {
        Map<Long, Integer> positionsByItemId = new HashMap<>();
        for (CourseStop stop : stops) {
            if (stop.id() != null) {
                positionsByItemId.put(stop.id(), stop.position());
            }
        }
        return positionsByItemId;
    }

    private static boolean hasCompleteSegmentSet(List<CourseStop> stops, List<CourseRouteSegment> segments) {
        if (stops.size() < 2) {
            return segments.isEmpty();
        }
        return segments.size() == stops.size() - 1;
    }

    private static void requirePlannedSegments(CourseRoute route) {
        if (!hasCompleteSegmentSet(route.stops(), route.segments())) {
            throw new CoreException(COURSE_INVALID_ITEM);
        }
    }

    private static CourseStopTarget target(CourseItemDetailRecord record) {
        if (TYPE_ATTRACTION.equals(record.itemType())) {
            return CourseStopTarget.attraction(record.attractionId());
        }
        if (TYPE_NOTE.equals(record.itemType())) {
            return CourseStopTarget.note(record.noteId());
        }
        throw new CoreException(COURSE_INVALID_ITEM);
    }

    private static int countValue(Integer value) {
        return value == null ? 0 : value;
    }

    private static String stringValue(LocalDateTime value) {
        return value == null ? "" : value.toString();
    }

}

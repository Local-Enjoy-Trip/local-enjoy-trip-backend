package com.ssafy.enjoytrip.core.domain;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_ACCESS_DENIED;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVALID_ITEM;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_NOT_FOUND;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemDetailRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRouteSegmentRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseReader {
    private static final String TYPE_ATTRACTION = CourseStopTargetType.ATTRACTION.name();
    private static final String TYPE_NOTE = CourseStopTargetType.NOTE.name();
    private static final int DEFAULT_FEED_LIMIT = 10;

    private final CourseMapper courseMapper;

    public List<Course> findMyCourses(String ownerUserId) {
        return courseMapper.findByOwnerUserId(ownerUserId).stream()
                .map(record -> findCourse(record, true, SegmentReadPolicy.USER_COURSE))
                .toList();
    }

    public Course findRequired(String id) {
        CourseRecord record = courseMapper.findById(id);
        requireExisting(record);
        return findCourse(record, true, SegmentReadPolicy.USER_COURSE);
    }

    public Course findPublicRequired(String id) {
        CourseRecord record = courseMapper.findById(id);
        if (record == null || record.getDeletedAt() != null || !isPublicReady(record)) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
        return findCourse(record, false, SegmentReadPolicy.PUBLIC_COURSE);
    }

    public List<Course> findAdminCourses() {
        return courseMapper.findAdminOwned().stream()
                .map(record -> findCourse(record, true, SegmentReadPolicy.PLANNED_COURSE))
                .toList();
    }

    public Course findRequiredOwned(String ownerUserId, String courseId) {
        CourseRecord record = courseMapper.findById(courseId);
        requireExisting(record);
        if (!ownerUserId.equals(record.getOwnerUserId())) {
            throw new CoreException(COURSE_ACCESS_DENIED);
        }
        return findCourse(record, true, SegmentReadPolicy.PLANNED_COURSE);
    }

    public List<CourseFeedSection> findPublicFeed() {
        return List.of(
                new CourseFeedSection(
                        "MD_RECOMMENDED",
                        "MD 추천",
                        "curationOrder",
                        publicCourses(courseMapper.findMdRecommendedPublic(DEFAULT_FEED_LIMIT))
                ),
                new CourseFeedSection(
                        "POPULAR",
                        "인기 코스",
                        "saveCountDesc",
                        publicCourses(courseMapper.findPopularPublic(DEFAULT_FEED_LIMIT))
                )
        );
    }

    private List<Course> publicCourses(List<CourseRecord> records) {
        return records.stream()
                .map(record -> findCourse(record, false, SegmentReadPolicy.PUBLIC_COURSE))
                .toList();
    }

    private Course findCourse(CourseRecord record,
                              boolean includePrivateItems,
                              SegmentReadPolicy segmentReadPolicy) {
        List<CourseItemDetailRecord> items = includePrivateItems
                ? courseMapper.findItemsByCourseId(record.getId())
                : courseMapper.findPublicItemsByCourseId(record.getId());
        return toCourse(
                record,
                items,
                courseMapper.findSegmentsByCourseId(record.getId()),
                segmentReadPolicy
        );
    }

    private static void requireExisting(CourseRecord record) {
        if (record == null || record.getDeletedAt() != null) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
    }

    private static Course toCourse(CourseRecord record,
                                   List<CourseItemDetailRecord> items,
                                   List<CourseRouteSegmentRecord> segments,
                                   SegmentReadPolicy segmentReadPolicy) {
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
                toRoute(items, segments, segmentReadPolicy)
        );
    }

    private static CourseRoute toRoute(List<CourseItemDetailRecord> items,
                                       List<CourseRouteSegmentRecord> segments,
                                       SegmentReadPolicy segmentReadPolicy) {
        List<CourseStop> stops = items.stream()
                .map(CourseReader::toStop)
                .toList();
        Map<Long, Integer> positionsByItemId = positionsByItemId(stops);
        List<CourseRouteSegment> routeSegments = segments.stream()
                .filter(segment -> positionsByItemId.containsKey(segment.getFromCourseItemId()))
                .filter(segment -> positionsByItemId.containsKey(segment.getToCourseItemId()))
                .map(segment -> toSegment(segment, positionsByItemId))
                .toList();
        if (routeSegments.isEmpty()) {
            if (segmentReadPolicy.requiresCompleteSegments()
                    && !hasCompleteSegmentSet(stops, routeSegments)) {
                throw new CoreException(COURSE_INVALID_ITEM);
            }
            return CourseRoute.ofStops(stops);
        }
        if (!hasCompleteSegmentSet(stops, routeSegments)) {
            if (!segmentReadPolicy.allowsIncompleteSegments()) {
                throw new CoreException(COURSE_INVALID_ITEM);
            }
            return CourseRoute.ofStops(stops);
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

    private static boolean isPublicReady(CourseRecord record) {
        return "PUBLIC".equals(record.getVisibility()) && "READY".equals(record.getStatus());
    }

    private enum SegmentReadPolicy {
        USER_COURSE,
        PUBLIC_COURSE,
        PLANNED_COURSE;

        private boolean requiresCompleteSegments() {
            return this == PLANNED_COURSE;
        }

        private boolean allowsIncompleteSegments() {
            return this == PUBLIC_COURSE;
        }
    }
}

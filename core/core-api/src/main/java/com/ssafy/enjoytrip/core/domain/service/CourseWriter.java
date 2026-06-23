package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVALID_ITEM;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseRoute;
import com.ssafy.enjoytrip.core.domain.CourseRouteSegment;
import com.ssafy.enjoytrip.core.domain.CourseStop;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRouteSegmentRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CourseWriter {
    private final CourseMapper courseMapper;

    @Transactional
    public void create(Course course, CourseRoute route) {
        requirePlannedSegments(route);
        courseMapper.insert(toRecord(course));
        replaceRoute(course.id(), route);
    }

    @Transactional
    public void update(Course course, CourseRoute route) {
        requirePlannedSegments(route);
        if (courseMapper.updateOwned(toRecord(course)) <= 0) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
        replaceRoute(course.id(), route);
    }

    @Transactional
    public void deleteOwned(String courseId, String ownerUserId) {
        if (courseMapper.softDeleteOwned(courseId, ownerUserId) <= 0) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
    }

    private void replaceRoute(String courseId, CourseRoute route) {
        courseMapper.deleteSegmentsByCourseId(courseId);
        courseMapper.deleteItemsByCourseId(courseId);
        for (CourseStop stop : route.stops()) {
            if (courseMapper.insertItem(toItemRecord(courseId, stop)) <= 0) {
                throw new CoreException(COURSE_INVALID_ITEM);
            }
        }

        Map<Integer, Long> itemIdsByPosition = reloadItemIdsByPosition(
                courseId,
                route.stops().size()
        );
        for (CourseRouteSegment segment : route.segments()) {
            CourseRouteSegmentRecord record = toSegmentRecord(
                    courseId,
                    segment,
                    itemIdsByPosition
            );
            if (courseMapper.insertSegment(record) <= 0) {
                throw new CoreException(COURSE_INVALID_ITEM);
            }
        }
    }

    private Map<Integer, Long> reloadItemIdsByPosition(String courseId, int expectedStopCount) {
        List<CourseItemRecord> records = courseMapper.findItemIdsByCourseId(courseId);
        if (records.size() != expectedStopCount) {
            throw new CoreException(COURSE_INVALID_ITEM);
        }

        Map<Integer, Long> itemIdsByPosition = new HashMap<>();
        for (CourseItemRecord record : records) {
            if (record.getId() == null || record.getPosition() == null) {
                throw new CoreException(COURSE_INVALID_ITEM);
            }
            Long previous = itemIdsByPosition.put(record.getPosition(), record.getId());
            if (previous != null) {
                throw new CoreException(COURSE_INVALID_ITEM);
            }
        }
        for (int position = 1; position <= expectedStopCount; position++) {
            if (!itemIdsByPosition.containsKey(position)) {
                throw new CoreException(COURSE_INVALID_ITEM);
            }
        }
        return itemIdsByPosition;
    }

    private static CourseItemRecord toItemRecord(String courseId, CourseStop stop) {
        return new CourseItemRecord(
                courseId,
                stop.target().type().name(),
                stop.target().attractionIdOrNull(),
                stop.target().noteIdOrNull(),
                stop.position(),
                stop.day(),
                stop.memo(),
                stop.stayMinutes()
        );
    }

    private static CourseRouteSegmentRecord toSegmentRecord(String courseId,
                                                            CourseRouteSegment segment,
                                                            Map<Integer, Long> itemIdsByPosition) {
        Long fromItemId = itemIdsByPosition.get(segment.fromPosition());
        Long toItemId = itemIdsByPosition.get(segment.toPosition());
        if (fromItemId == null || toItemId == null) {
            throw new CoreException(COURSE_INVALID_ITEM);
        }
        return new CourseRouteSegmentRecord(
                courseId,
                fromItemId,
                toItemId,
                segment.segmentOrder(),
                segment.travelMode(),
                segment.durationSeconds(),
                segment.distanceMeters()
        );
    }

    private static CourseRecord toRecord(Course course) {
        return new CourseRecord(
                course.id(),
                course.ownerUserId(),
                course.title(),
                course.regionName(),
                course.visibility(),
                course.status(),
                course.description(),
                course.coverImageUrl(),
                course.curationSection(),
                course.curationOrder()
        );
    }

    private static void requirePlannedSegments(CourseRoute route) {
        if (!hasCompleteSegmentSet(route)) {
            throw new CoreException(COURSE_INVALID_ITEM);
        }
    }

    private static boolean hasCompleteSegmentSet(CourseRoute route) {
        if (route.stops().size() < 2) {
            return route.segments().isEmpty();
        }
        return route.segments().size() == route.stops().size() - 1;
    }
}

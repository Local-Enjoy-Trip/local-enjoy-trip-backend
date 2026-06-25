package com.ssafy.enjoytrip.core.domain;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVALID_ITEM;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_NOT_FOUND;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseEmbeddingMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CourseWriter {
    private final CourseMapper courseMapper;
    private final CourseEmbeddingMapper courseEmbeddingMapper;
    private final CourseStopPointResolver courseStopPointResolver;
    private final CourseRoutePlanner courseRoutePlanner;

    @Transactional
    public Course create(Course course) {
        List<CourseStopPoint> points = courseStopPointResolver.resolveAll(course.stops());
        List<CourseStop> plannedStops = courseRoutePlanner.plan(points);
        CourseStopPoint startPoint = firstPoint(points);

        courseMapper.insert(toRecord(course));
        updateStartLocation(course.id(), startPoint);
        saveCourseTags(course.id(), course.tags());

        Course created = course.withStartLocation(startPoint)
                .withStops(saveStops(course.id(), plannedStops));
        courseMapper.updateThumbnailUrl(course.id());
        if (plannedStops.size() >= 2) {
            courseEmbeddingMapper.markPending(course.id());
        }
        return created;
    }

    @Transactional
    public Course update(Course course) {
        List<CourseStopPoint> points = courseStopPointResolver.resolveAll(course.stops());
        List<CourseStop> plannedStops = courseRoutePlanner.plan(points);
        CourseStopPoint startPoint = firstPoint(points);

        if (courseMapper.updateOwned(toRecord(course)) <= 0) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
        updateStartLocation(course.id(), startPoint);
        courseMapper.deleteTagsByCourseId(course.id());
        saveCourseTags(course.id(), course.tags());

        courseMapper.deleteItemsByCourseId(course.id());
        Course updated = course.withStartLocation(startPoint)
                .withStops(saveStops(course.id(), plannedStops));
        courseMapper.updateThumbnailUrl(course.id());
        if (plannedStops.size() >= 2) {
            courseEmbeddingMapper.markPending(course.id());
        }
        return updated;
    }

    @Transactional
    public void deleteOwned(String courseId, Long ownerMemberId) {
        if (courseMapper.softDeleteOwned(courseId, ownerMemberId) <= 0) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
    }

    @Transactional
    public void save(String courseId, Long memberId) {
        courseMapper.insertSave(courseId, memberId);
    }

    @Transactional
    public void unsave(String courseId, Long memberId) {
        courseMapper.deleteSave(courseId, memberId);
    }

    private void updateStartLocation(String courseId, CourseStopPoint startPoint) {
        Double longitude = startPoint == null ? null : startPoint.longitude();
        Double latitude = startPoint == null ? null : startPoint.latitude();
        if (courseMapper.updateStartLocation(courseId, longitude, latitude) <= 0) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
    }

    private void saveCourseTags(String courseId, List<CourseTag> tags) {
        for (CourseTag tag : tags) {
            courseMapper.insertCourseTag(courseId, tag.tagId());
        }
    }

    private List<CourseStop> saveStops(String courseId, List<CourseStop> stops) {
        if (stops.isEmpty()) {
            return List.of();
        }

        List<CourseItemRecord> records = stops.stream()
                .map(stop -> toItemRecord(courseId, stop))
                .toList();
        if (courseMapper.insertItems(records) != records.size()) {
            throw new CoreException(COURSE_INVALID_ITEM);
        }

        List<CourseStop> savedStops = new ArrayList<>();
        for (int index = 0; index < stops.size(); index++) {
            CourseItemRecord record = records.get(index);
            if (record.getId() == null) {
                throw new CoreException(COURSE_INVALID_ITEM);
            }
            savedStops.add(stops.get(index).withId(record.getId()));
        }
        return savedStops;
    }

    private static CourseStopPoint firstPoint(List<CourseStopPoint> points) {
        return points.isEmpty() ? null : points.get(0);
    }

    private static CourseRecord toRecord(Course course) {
        return new CourseRecord(
                course.id(),
                course.ownerMemberId(),
                course.title(),
                course.regionName(),
                course.date()
        );
    }

    private static CourseItemRecord toItemRecord(String courseId, CourseStop stop) {
        return new CourseItemRecord(
                courseId,
                stop.target().type().name(),
                stop.target().attractionIdOrNull(),
                stop.target().noteIdOrNull(),
                stop.position(),
                stop.distanceToNext(),
                stop.durationToNext()
        );
    }
}

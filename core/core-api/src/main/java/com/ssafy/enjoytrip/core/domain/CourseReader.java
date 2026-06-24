package com.ssafy.enjoytrip.core.domain;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_ACCESS_DENIED;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVALID_ITEM;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.query.DistanceSearchCondition;
import com.ssafy.enjoytrip.core.domain.vo.Coordinate;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemDetailRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseTagRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseReader {
    private static final String TYPE_ATTRACTION = CourseStopTargetType.ATTRACTION.name();
    private static final String TYPE_NOTE = CourseStopTargetType.NOTE.name();
    private final CourseMapper courseMapper;

    public List<Course> findMyCourses(Long ownerMemberId) {
        return courseMapper.findByOwnerMemberId(ownerMemberId).stream()
                .map(record -> findCourse(record, true))
                .toList();
    }

    public Course findRequired(String id) {
        CourseRecord record = courseMapper.findById(id);
        requireExisting(record);
        return findCourse(record, true);
    }

    public Course findPublicRequired(String id) {
        CourseRecord record = courseMapper.findById(id);
        if (record == null || record.getDeletedAt() != null) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
        return findCourse(record, false);
    }

    public Course findRequiredOwned(Long ownerMemberId, String courseId) {
        CourseRecord record = courseMapper.findById(courseId);
        requireExisting(record);
        if (!ownerMemberId.equals(record.getOwnerMemberId())) {
            throw new CoreException(COURSE_ACCESS_DENIED);
        }
        return findCourse(record, true);
    }

    public List<Course> findPublicFeed(DistanceSearchCondition condition) {
        return courseMapper.findDistanceOrderedPublicFeed(
                condition.longitude(),
                condition.latitude(),
                condition.limit(),
                condition.radiusMeters()
        ).stream()
                .map(record -> findCourse(record, false))
                .toList();
    }

    public List<Course> findPopularByRegion(String regionName, int limit) {
        return courseMapper.findByRegionOrderedBySaveCount(regionName, limit).stream()
                .map(record -> findCourse(record, false))
                .toList();
    }

    public List<Course> findAllBySaveCount(int limit) {
        return courseMapper.findAllBySaveCount(limit).stream()
                .map(record -> findCourse(record, false))
                .toList();
    }

    public long countMemberFavorites(Long memberId) {
        return courseMapper.countMemberFavorites(memberId);
    }

    public List<Course> findRecommended(Long memberId, int limit) {
        return courseMapper.findRecommendedCourses(memberId, limit).stream()
                .map(record -> findCourse(record, false))
                .toList();
    }

    private Course findCourse(CourseRecord record, boolean includePrivateItems) {
        List<CourseItemDetailRecord> items = includePrivateItems
                ? courseMapper.findItemsByCourseId(record.getId())
                : courseMapper.findPublicItemsByCourseId(record.getId());
        List<CourseTagRecord> tagRecords = courseMapper.findTagsByCourseId(record.getId());
        return toCourse(record, items, tagRecords);
    }

    private static void requireExisting(CourseRecord record) {
        if (record == null || record.getDeletedAt() != null) {
            throw new CoreException(COURSE_NOT_FOUND);
        }
    }

    private static Course toCourse(
            CourseRecord record,
            List<CourseItemDetailRecord> items,
            List<CourseTagRecord> tagRecords
    ) {
        Coordinate startLocation = null;
        if (record.getStartLatitude() != null && record.getStartLongitude() != null) {
            startLocation = new Coordinate(record.getStartLatitude(), record.getStartLongitude());
        }

        List<CourseTag> tags = tagRecords.stream()
                .map(t -> new CourseTag(t.tagId(), t.tagName()))
                .toList();

        return new Course(
                record.getId(),
                record.getOwnerMemberId(),
                record.getTitle(),
                record.getRegionName(),
                record.getDate(),
                startLocation,
                record.getDistanceMeters(),
                countValue(record.getSaveCount()),
                stringValue(record.getCreatedAt()),
                stringValue(record.getUpdatedAt()),
                items.stream()
                        .map(CourseReader::toStop)
                        .toList(),
                tags
        );
    }

    private static CourseStop toStop(CourseItemDetailRecord record) {
        return new CourseStop(
                record.id(),
                target(record),
                countValue(record.position()),
                record.itemTitle(),
                record.distanceToNext(),
                record.durationToNext()
        );
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

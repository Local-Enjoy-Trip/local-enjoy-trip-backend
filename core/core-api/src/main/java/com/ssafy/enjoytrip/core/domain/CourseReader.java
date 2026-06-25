package com.ssafy.enjoytrip.core.domain;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_ACCESS_DENIED;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVALID_ITEM;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.query.DistanceSearchCondition;
import com.ssafy.enjoytrip.core.domain.vo.Coordinate;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemDetailRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecommendationCandidateRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseTagRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseInvitationMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseReader {
    private static final String TYPE_ATTRACTION = CourseStopTargetType.ATTRACTION.name();
    private static final String TYPE_NOTE = CourseStopTargetType.NOTE.name();
    private final CourseMapper courseMapper;
    private final CourseInvitationMapper courseInvitationMapper;

    public List<Course> findMyCourses(Long memberId) {
        List<Course> owned = courseMapper.findByOwnerMemberId(memberId).stream()
                .map(record -> findCourse(record, true))
                .toList();

        List<Course> invited = courseInvitationMapper.findAcceptedCourseIdsByInvitee(memberId).stream()
                .map(courseMapper::findById)
                .filter(record -> record != null && record.getDeletedAt() == null)
                .map(record -> findCourse(record, false))
                .toList();

        List<Course> result = new ArrayList<>(owned);
        result.addAll(invited);
        return result;
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

    public List<Course> findPopularByRegionPrefix(String prefix, int limit) {
        return courseMapper.findByRegionPrefixOrderedBySaveCount(prefix, limit).stream()
                .map(record -> findCourse(record, false))
                .toList();
    }

    public List<Course> findAllBySaveCount(int limit) {
        return courseMapper.findAllBySaveCount(limit).stream()
                .map(record -> findCourse(record, false))
                .toList();
    }

    public boolean hasMemberProfileEmbedding(Long memberId) {
        return courseMapper.hasMemberProfileEmbedding(memberId);
    }

    public List<CourseRecommendationCandidate> findCandidatesByMemberProfile(
            Long memberId,
            int candidateLimit
    ) {
        List<CourseRecommendationCandidateRecord> candidates =
                courseMapper.findCandidatesByMemberProfile(memberId, candidateLimit);

        if (candidates.isEmpty()) {
            return List.of();
        }

        List<String> courseIds = candidates.stream()
                .map(CourseRecommendationCandidateRecord::getId)
                .toList();

        Map<String, List<CourseItemDetailRecord>> itemsByCourseId =
                courseMapper.findPublicItemsByCourseIds(courseIds).stream()
                        .collect(Collectors.groupingBy(CourseItemDetailRecord::courseId));

        Map<String, List<CourseTagRecord>> tagsByCourseId =
                courseMapper.findTagsByCourseIds(courseIds).stream()
                        .collect(Collectors.groupingBy(CourseTagRecord::courseId));

        return candidates.stream()
                .map(r -> {
                    List<CourseItemDetailRecord> items =
                            itemsByCourseId.getOrDefault(r.getId(), List.of());
                    List<CourseTagRecord> tags =
                            tagsByCourseId.getOrDefault(r.getId(), List.of());
                    return new CourseRecommendationCandidate(
                            toCourseFromCandidate(r, items, tags),
                            r.getDominantCategory(),
                            r.getSimilarityDistance() != null ? r.getSimilarityDistance() : 1.0
                    );
                })
                .toList();
    }

    public List<String> findRecentlyViewedCourseIds(Long memberId, int days) {
        return courseMapper.findRecentlyViewedCourseIds(memberId, days);
    }

    private Course toCourseFromCandidate(
            CourseRecommendationCandidateRecord r,
            List<CourseItemDetailRecord> items,
            List<CourseTagRecord> tagRecords
    ) {
        List<CourseTag> tags = tagRecords.stream()
                .map(t -> new CourseTag(t.tagId(), t.tagName()))
                .toList();
        return new Course(
                r.getId(),
                r.getOwnerMemberId(),
                r.getTitle(),
                r.getRegionName(),
                r.getDate(),
                null,
                null,
                r.getSaveCount() != null ? r.getSaveCount() : 0,
                r.getThumbnailUrl(),
                stringValue(r.getCreatedAt()),
                stringValue(r.getUpdatedAt()),
                items.stream().map(CourseReader::toStop).toList(),
                tags
        );
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
                record.getThumbnailUrl(),
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

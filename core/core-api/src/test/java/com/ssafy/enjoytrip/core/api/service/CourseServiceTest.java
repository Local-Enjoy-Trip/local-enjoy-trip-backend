package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVALID_ITEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseRoute;
import com.ssafy.enjoytrip.core.domain.CourseStop;
import com.ssafy.enjoytrip.core.domain.CourseStopTarget;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemDetailRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRouteSegmentRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class CourseServiceTest {
    private CourseMapper courseMapper;
    private AttractionMapper attractionMapper;
    private NoteMapper noteMapper;
    private CourseService service;

    @BeforeEach
    void setUp() {
        courseMapper = Mockito.mock(CourseMapper.class);
        attractionMapper = Mockito.mock(AttractionMapper.class);
        noteMapper = Mockito.mock(NoteMapper.class);
        service = new CourseService(
                courseMapper,
                new CourseStopPointResolver(attractionMapper, noteMapper),
                new DefaultCourseRoutePlanner(),
                new CourseWriter(courseMapper)
        );
    }

    @DisplayName("코스 생성은 숨김 장소나 비공개 노트를 항목으로 저장하지 않는다")
    @Test
    void createCourseRejectsNonPublicItems() {
        Course course = course("course-1", "user", "PRIVATE", "READY", attractionStop(10L, 1));
        when(attractionMapper.existsPublicVisibleById(10L)).thenReturn(0);

        assertThatThrownBy(() -> service.createCourse(course))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(COURSE_INVALID_ITEM));

        verify(courseMapper, never()).insert(any(CourseRecord.class));
    }

    @DisplayName("코스 생성은 장소 항목 저장 시 attraction_id만 채운다")
    @Test
    void createCoursePersistsAttractionTargetOnly() {
        Course course = course("course-attraction", "user", "PRIVATE", "READY", attractionStop(10L, 1));
        stubAttraction(10L, 37.0, 127.0);
        when(courseMapper.insertItem(any(CourseItemRecord.class))).thenReturn(1);
        when(courseMapper.findItemIdsByCourseId("course-attraction")).thenReturn(List.of(
                itemId("course-attraction", 101L, 1)
        ));
        when(courseMapper.findById("course-attraction")).thenReturn(
                courseRecord("course-attraction", "user", null, null, 0)
        );
        when(courseMapper.findItemsByCourseId("course-attraction")).thenReturn(List.of(
                itemDetail(101L, "course-attraction", 10L, 1, "장소 10")
        ));
        when(courseMapper.findSegmentsByCourseId("course-attraction")).thenReturn(List.of());

        Course created = service.createCourse(course);

        assertThat(created.route().stops()).extracting(stop -> stop.target().id()).containsExactly(10L);
        ArgumentCaptor<CourseItemRecord> itemCaptor = ArgumentCaptor.forClass(CourseItemRecord.class);
        verify(courseMapper).insertItem(itemCaptor.capture());
        CourseItemRecord insertedItem = itemCaptor.getValue();
        assertThat(insertedItem.getItemType()).isEqualTo("ATTRACTION");
        assertThat(insertedItem.getAttractionId()).isEqualTo(10L);
        assertThat(insertedItem.getNoteId()).isNull();
    }

    @DisplayName("코스 생성은 쪽지 항목 저장 시 note_id만 채운다")
    @Test
    void createCoursePersistsNoteTargetOnly() {
        Course course = course("course-note", "user", "PRIVATE", "READY", noteStop(30L, 1));
        stubNote(30L, 37.0, 127.0);
        when(courseMapper.insertItem(any(CourseItemRecord.class))).thenReturn(1);
        when(courseMapper.findItemIdsByCourseId("course-note")).thenReturn(List.of(
                itemId("course-note", 301L, 1)
        ));
        when(courseMapper.findById("course-note")).thenReturn(
                courseRecord("course-note", "user", null, null, 0)
        );
        when(courseMapper.findItemsByCourseId("course-note")).thenReturn(List.of(
                noteItemDetail(301L, "course-note", 30L, 1, "쪽지 30")
        ));
        when(courseMapper.findSegmentsByCourseId("course-note")).thenReturn(List.of());

        Course created = service.createCourse(course);

        assertThat(created.route().stops()).extracting(stop -> stop.target().id()).containsExactly(30L);
        ArgumentCaptor<CourseItemRecord> itemCaptor = ArgumentCaptor.forClass(CourseItemRecord.class);
        verify(courseMapper).insertItem(itemCaptor.capture());
        CourseItemRecord insertedItem = itemCaptor.getValue();
        assertThat(insertedItem.getItemType()).isEqualTo("NOTE");
        assertThat(insertedItem.getAttractionId()).isNull();
        assertThat(insertedItem.getNoteId()).isEqualTo(30L);
    }

    @DisplayName("코스 생성은 비공개 쪽지를 항목으로 저장하지 않는다")
    @Test
    void createCourseRejectsNonPublicNote() {
        Course course = course("course-private-note", "user", "PRIVATE", "READY", noteStop(30L, 1));
        when(noteMapper.existsPublicActive(30L)).thenReturn(0);

        assertThatThrownBy(() -> service.createCourse(course))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(COURSE_INVALID_ITEM));

        verify(courseMapper, never()).insert(any(CourseRecord.class));
    }

    @DisplayName("코스 생성은 위치로 재조회한 아이템 id로 인접 구간을 저장한다")
    @Test
    void createCoursePersistsSegmentsWithReloadedItemIds() {
        Course course = course(
                "course-1",
                "user",
                "PRIVATE",
                "READY",
                attractionStop(10L, 1),
                attractionStop(20L, 2)
        );
        stubAttraction(10L, 37.0, 127.0);
        stubAttraction(20L, 37.1, 127.1);
        stubSuccessfulRouteWrite("course-1", 101L, 102L);
        stubFoundCourseWithSegment("course-1", "user", 101L, 102L, 10L, 20L);

        Course created = service.createCourse(course);

        assertThat(created.route().segments()).hasSize(1);
        assertThat(created.routeSummary().segmentCount()).isEqualTo(1);
        verify(courseMapper).deleteSegmentsByCourseId("course-1");
        verify(courseMapper).deleteItemsByCourseId("course-1");
        verify(courseMapper).insertSegment(any(CourseRouteSegmentRecord.class));
    }

    @DisplayName("중복 대상 경유지도 position 재조회로 서로 다른 구간 id에 매핑한다")
    @Test
    void createCoursePersistsDuplicateTargetsByPosition() {
        Course course = course(
                "course-duplicate",
                "user",
                "PRIVATE",
                "READY",
                attractionStop(10L, 1),
                attractionStop(10L, 2)
        );
        stubAttraction(10L, 37.0, 127.0);
        stubSuccessfulRouteWrite("course-duplicate", 201L, 202L);
        stubFoundCourseWithSegment("course-duplicate", "user", 201L, 202L, 10L, 10L);

        Course created = service.createCourse(course);

        assertThat(created.route().stops()).extracting(stop -> stop.target().id()).containsExactly(10L, 10L);
        verify(courseMapper).insertSegment(any(CourseRouteSegmentRecord.class));
    }

    @DisplayName("2개 이상 경유지의 좌표가 없으면 코스 생성은 저장 전에 실패한다")
    @Test
    void createCourseRejectsMissingCoordinatesBeforeInsert() {
        Course course = course(
                "course-missing-coordinate",
                "user",
                "PRIVATE",
                "READY",
                attractionStop(10L, 1),
                attractionStop(20L, 2)
        );
        stubAttraction(10L, 37.0, 127.0);
        stubAttraction(20L, null, 127.1);

        assertThatThrownBy(() -> service.createCourse(course))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(COURSE_INVALID_ITEM));

        verify(courseMapper, never()).insert(any(CourseRecord.class));
    }

    @DisplayName("아이템 id 재조회 결과가 경유지 수와 다르면 코스 생성은 실패한다")
    @Test
    void createCourseRejectsReloadMismatch() {
        Course course = course(
                "course-reload-mismatch",
                "user",
                "PRIVATE",
                "READY",
                attractionStop(10L, 1),
                attractionStop(20L, 2)
        );
        stubAttraction(10L, 37.0, 127.0);
        stubAttraction(20L, 37.1, 127.1);
        when(courseMapper.insertItem(any(CourseItemRecord.class))).thenReturn(1);
        when(courseMapper.findItemIdsByCourseId("course-reload-mismatch")).thenReturn(List.of(
                itemId("course-reload-mismatch", 101L, 1)
        ));

        assertThatThrownBy(() -> service.createCourse(course))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(COURSE_INVALID_ITEM));

        verify(courseMapper, never()).insertSegment(any(CourseRouteSegmentRecord.class));
    }

    @DisplayName("구간 insert가 실패하면 코스 생성은 실패한다")
    @Test
    void createCourseRejectsSegmentInsertFailure() {
        Course course = course(
                "course-segment-failure",
                "user",
                "PRIVATE",
                "READY",
                attractionStop(10L, 1),
                attractionStop(20L, 2)
        );
        stubAttraction(10L, 37.0, 127.0);
        stubAttraction(20L, 37.1, 127.1);
        when(courseMapper.insertItem(any(CourseItemRecord.class))).thenReturn(1);
        when(courseMapper.findItemIdsByCourseId("course-segment-failure")).thenReturn(List.of(
                itemId("course-segment-failure", 101L, 1),
                itemId("course-segment-failure", 102L, 2)
        ));
        when(courseMapper.insertSegment(any(CourseRouteSegmentRecord.class))).thenReturn(0);

        assertThatThrownBy(() -> service.createCourse(course))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(COURSE_INVALID_ITEM));
    }

    @DisplayName("코스 수정은 기존 구간을 지우고 새로 계획한 구간으로 교체한다")
    @Test
    void updateCourseReplacesExistingSegments() {
        Course course = course(
                "course-1",
                "user",
                "PRIVATE",
                "READY",
                attractionStop(10L, 1),
                attractionStop(20L, 2)
        );
        stubAttraction(10L, 37.0, 127.0);
        stubAttraction(20L, 37.1, 127.1);
        when(courseMapper.findById("course-1")).thenReturn(courseRecord("course-1", "user", null, null, 0));
        when(courseMapper.findItemsByCourseId("course-1")).thenReturn(List.of(), List.of(
                itemDetail(101L, "course-1", 10L, 1, "첫 장소"),
                itemDetail(102L, "course-1", 20L, 2, "두 번째 장소")
        ));
        when(courseMapper.findSegmentsByCourseId("course-1")).thenReturn(List.of(), List.of(
                new CourseRouteSegmentRecord("course-1", 101L, 102L, 1, "WALK", 100, 140)
        ));
        when(courseMapper.updateOwned(any(CourseRecord.class))).thenReturn(1);
        stubSuccessfulRouteWrite("course-1", 101L, 102L);

        Course updated = service.updateCourse("user", course);

        assertThat(updated.routeSummary().segmentCount()).isEqualTo(1);
        verify(courseMapper).updateOwned(any(CourseRecord.class));
        verify(courseMapper).deleteSegmentsByCourseId("course-1");
        verify(courseMapper).deleteItemsByCourseId("course-1");
    }

    @DisplayName("공개 피드는 MD 추천과 인기 코스를 섹션별로 반환한다")
    @Test
    void publicFeedReturnsSectionedCourses() {
        when(courseMapper.findMdRecommendedPublic(10)).thenReturn(List.of(
                courseRecord("md-1", "admin", "MD_RECOMMENDED", 1, 0)
        ));
        when(courseMapper.findPopularPublic(10)).thenReturn(List.of(
                courseRecord("popular-1", "admin", null, null, 3)
        ));
        when(courseMapper.findPublicItemsByCourseId(eq("md-1"))).thenReturn(List.of());
        when(courseMapper.findPublicItemsByCourseId(eq("popular-1"))).thenReturn(List.of());
        when(courseMapper.findSegmentsByCourseId(any(String.class))).thenReturn(List.of());

        List<CourseFeedSection> feed = service.findPublicFeed();

        assertThat(feed).hasSize(2);
        assertThat(feed.get(0).key()).isEqualTo("MD_RECOMMENDED");
        assertThat(feed.get(0).courses()).extracting(Course::id).containsExactly("md-1");
        assertThat(feed.get(1).key()).isEqualTo("POPULAR");
        assertThat(feed.get(1).courses()).extracting(Course::saveCount).containsExactly(3);
    }

    private void stubAttraction(Long attractionId, Double latitude, Double longitude) {
        when(attractionMapper.existsPublicVisibleById(attractionId)).thenReturn(1);
        when(attractionMapper.findByIds(List.of(attractionId))).thenReturn(List.of(
                attraction(attractionId, latitude, longitude)
        ));
    }

    private void stubNote(Long noteId, Double latitude, Double longitude) {
        when(noteMapper.existsPublicActive(noteId)).thenReturn(1);
        when(noteMapper.findById(noteId)).thenReturn(note(noteId, latitude, longitude));
    }

    private void stubSuccessfulRouteWrite(String courseId, Long firstItemId, Long secondItemId) {
        when(courseMapper.insertItem(any(CourseItemRecord.class))).thenReturn(1);
        when(courseMapper.insertSegment(any(CourseRouteSegmentRecord.class))).thenReturn(1);
        when(courseMapper.findItemIdsByCourseId(courseId)).thenReturn(List.of(
                itemId(courseId, firstItemId, 1),
                itemId(courseId, secondItemId, 2)
        ));
    }

    private void stubFoundCourseWithSegment(String courseId,
                                            String ownerUserId,
                                            Long firstItemId,
                                            Long secondItemId,
                                            Long firstAttractionId,
                                            Long secondAttractionId) {
        when(courseMapper.findById(courseId)).thenReturn(courseRecord(courseId, ownerUserId, null, null, 0));
        when(courseMapper.findItemsByCourseId(courseId)).thenReturn(List.of(
                itemDetail(firstItemId, courseId, firstAttractionId, 1, "첫 장소"),
                itemDetail(secondItemId, courseId, secondAttractionId, 2, "두 번째 장소")
        ));
        when(courseMapper.findSegmentsByCourseId(courseId)).thenReturn(List.of(
                new CourseRouteSegmentRecord(courseId, firstItemId, secondItemId, 1, "WALK", 100, 140)
        ));
    }

    private static Course course(String id,
                                 String ownerUserId,
                                 String visibility,
                                 String status,
                                 CourseStop... stops) {
        return new Course(
                id,
                ownerUserId,
                id,
                "서울",
                visibility,
                status,
                null,
                null,
                null,
                null,
                0,
                "",
                "",
                CourseRoute.ofStops(List.of(stops))
        );
    }

    private static CourseStop attractionStop(Long attractionId, int position) {
        return new CourseStop(
                null,
                CourseStopTarget.attraction(attractionId),
                position,
                1,
                null,
                null,
                null
        );
    }

    private static CourseStop noteStop(Long noteId, int position) {
        return new CourseStop(
                null,
                CourseStopTarget.note(noteId),
                position,
                1,
                null,
                null,
                null
        );
    }

    private static AttractionRecord attraction(Long id, Double latitude, Double longitude) {
        return new AttractionRecord(
                id,
                "장소 " + id,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                null,
                null,
                latitude,
                longitude,
                null,
                null,
                null
        );
    }

    private static NoteRecord note(Long id, Double latitude, Double longitude) {
        return new NoteRecord(
                id,
                "author",
                "쪽지 " + id,
                "내용",
                "TIP",
                "PUBLIC",
                bigDecimal(latitude),
                bigDecimal(longitude),
                "서울",
                null,
                null,
                null
        );
    }

    private static BigDecimal bigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static CourseItemRecord itemId(String courseId, Long id, Integer position) {
        CourseItemRecord record = new CourseItemRecord(
                courseId,
                "ATTRACTION",
                id,
                null,
                position,
                1,
                null,
                null
        );
        record.setId(id);
        return record;
    }

    private static CourseItemDetailRecord itemDetail(Long id,
                                                     String courseId,
                                                     Long attractionId,
                                                     Integer position,
                                                     String title) {
        return new CourseItemDetailRecord(
                id,
                courseId,
                "ATTRACTION",
                attractionId,
                null,
                position,
                1,
                null,
                null,
                title,
                null,
                title,
                null,
                null
        );
    }

    private static CourseItemDetailRecord noteItemDetail(Long id,
                                                         String courseId,
                                                         Long noteId,
                                                         Integer position,
                                                         String title) {
        return new CourseItemDetailRecord(
                id,
                courseId,
                "NOTE",
                null,
                noteId,
                position,
                1,
                null,
                null,
                null,
                title,
                title,
                null,
                null
        );
    }

    private static CourseRecord courseRecord(String id,
                                             String ownerUserId,
                                             String curationSection,
                                             Integer curationOrder,
                                             Integer saveCount) {
        CourseRecord record = new CourseRecord(
                id,
                ownerUserId,
                id,
                "서울",
                "PUBLIC",
                "READY",
                null,
                null,
                curationSection,
                curationOrder
        );
        record.setSaveCount(saveCount);
        return record;
    }
}

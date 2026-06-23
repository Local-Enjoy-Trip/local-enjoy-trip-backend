package com.ssafy.enjoytrip.core.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class AdminCourseServiceTest {
    private CourseMapper courseMapper;
    private MemberMapper memberMapper;
    private AttractionMapper attractionMapper;
    private AdminCourseService service;
    private NoteMapper noteMapper;

    @BeforeEach
    void setUp() {
        courseMapper = Mockito.mock(CourseMapper.class);
        memberMapper = Mockito.mock(MemberMapper.class);
        attractionMapper = Mockito.mock(AttractionMapper.class);
        noteMapper = Mockito.mock(NoteMapper.class);
        service = new AdminCourseService(
                courseMapper,
                memberMapper,
                new CourseStopPointResolver(attractionMapper, noteMapper),
                new DefaultCourseRoutePlanner(),
                new CourseWriter(courseMapper)
        );
    }

    @DisplayName("관리자 코스 생성도 계획된 경로를 저장하고 구간을 재구성한다")
    @Test
    void createAdminCoursePersistsAndReadsPlannedRoute() {
        Course course = course(
                "admin-course",
                "admin",
                attractionStop(10L, 1),
                attractionStop(20L, 2)
        );
        when(memberMapper.findByUserId("admin")).thenReturn(adminMember());
        stubAttraction(10L, 37.0, 127.0);
        stubAttraction(20L, 37.1, 127.1);
        when(courseMapper.insertItem(any(CourseItemRecord.class))).thenReturn(1);
        when(courseMapper.insertSegment(any(CourseRouteSegmentRecord.class))).thenReturn(1);
        when(courseMapper.findItemIdsByCourseId("admin-course")).thenReturn(List.of(
                itemId("admin-course", 301L, 1),
                itemId("admin-course", 302L, 2)
        ));
        when(courseMapper.findById("admin-course")).thenReturn(
                courseRecord("admin-course", "admin")
        );
        when(courseMapper.findItemsByCourseId("admin-course")).thenReturn(List.of(
                itemDetail(301L, "admin-course", 10L, 1, "첫 장소"),
                itemDetail(302L, "admin-course", 20L, 2, "두 번째 장소")
        ));
        when(courseMapper.findSegmentsByCourseId("admin-course")).thenReturn(List.of(
                new CourseRouteSegmentRecord("admin-course", 301L, 302L, 1, "WALK", 100, 140)
        ));

        Course created = service.createAdminCourse(course);

        assertThat(created.routeSummary().segmentCount()).isEqualTo(1);
        verify(courseMapper).deleteSegmentsByCourseId("admin-course");
        verify(courseMapper).deleteItemsByCourseId("admin-course");
        verify(courseMapper).insertSegment(any(CourseRouteSegmentRecord.class));
    }

    @DisplayName("관리자 코스 생성은 쪽지 항목 저장 시 note_id만 채운다")
    @Test
    void createAdminCoursePersistsNoteTargetOnly() {
        Course course = course("admin-note", "admin", noteStop(30L, 1));
        when(memberMapper.findByUserId("admin")).thenReturn(adminMember());
        stubNote(30L, 37.0, 127.0);
        when(courseMapper.insertItem(any(CourseItemRecord.class))).thenReturn(1);
        when(courseMapper.findItemIdsByCourseId("admin-note")).thenReturn(List.of(
                itemId("admin-note", 401L, 1)
        ));
        when(courseMapper.findById("admin-note")).thenReturn(
                courseRecord("admin-note", "admin")
        );
        when(courseMapper.findItemsByCourseId("admin-note")).thenReturn(List.of(
                noteItemDetail(401L, "admin-note", 30L, 1, "쪽지 30")
        ));
        when(courseMapper.findSegmentsByCourseId("admin-note")).thenReturn(List.of());

        service.createAdminCourse(course);

        ArgumentCaptor<CourseItemRecord> itemCaptor = ArgumentCaptor.forClass(CourseItemRecord.class);
        verify(courseMapper).insertItem(itemCaptor.capture());
        CourseItemRecord insertedItem = itemCaptor.getValue();
        assertThat(insertedItem.getItemType()).isEqualTo("NOTE");
        assertThat(insertedItem.getAttractionId()).isNull();
        assertThat(insertedItem.getNoteId()).isEqualTo(30L);
    }

    @DisplayName("관리자 코스 생성은 숨김 장소를 항목으로 저장하지 않는다")
    @Test
    void createAdminCourseRejectsHiddenAttraction() {
        Course course = course("admin-hidden", "admin", attractionStop(10L, 1));
        when(memberMapper.findByUserId("admin")).thenReturn(adminMember());
        when(attractionMapper.existsPublicVisibleById(10L)).thenReturn(0);

        assertThatThrownBy(() -> service.createAdminCourse(course))
                .isInstanceOf(CoreException.class);

        verify(courseMapper, never()).insert(any(CourseRecord.class));
    }

    @DisplayName("관리자 코스 수정도 계획된 경로로 기존 항목과 구간을 교체한다")
    @Test
    void updateAdminCourseReplacesExistingSegments() {
        Course course = course(
                "admin-update",
                "admin",
                attractionStop(10L, 1),
                attractionStop(20L, 2)
        );
        when(memberMapper.findByUserId("admin")).thenReturn(adminMember());
        stubAttraction(10L, 37.0, 127.0);
        stubAttraction(20L, 37.1, 127.1);
        when(courseMapper.findById("admin-update")).thenReturn(
                courseRecord("admin-update", "admin"),
                courseRecord("admin-update", "admin")
        );
        when(courseMapper.findItemsByCourseId("admin-update")).thenReturn(List.of(), List.of(
                itemDetail(501L, "admin-update", 10L, 1, "첫 장소"),
                itemDetail(502L, "admin-update", 20L, 2, "두 번째 장소")
        ));
        when(courseMapper.findSegmentsByCourseId("admin-update")).thenReturn(List.of(), List.of(
                new CourseRouteSegmentRecord("admin-update", 501L, 502L, 1, "WALK", 100, 140)
        ));
        when(courseMapper.updateOwned(any(CourseRecord.class))).thenReturn(1);
        when(courseMapper.insertItem(any(CourseItemRecord.class))).thenReturn(1);
        when(courseMapper.insertSegment(any(CourseRouteSegmentRecord.class))).thenReturn(1);
        when(courseMapper.findItemIdsByCourseId("admin-update")).thenReturn(List.of(
                itemId("admin-update", 501L, 1),
                itemId("admin-update", 502L, 2)
        ));

        Course updated = service.updateAdminCourse("admin", course);

        assertThat(updated.routeSummary().segmentCount()).isEqualTo(1);
        verify(courseMapper).updateOwned(any(CourseRecord.class));
        verify(courseMapper).deleteSegmentsByCourseId("admin-update");
        verify(courseMapper).deleteItemsByCourseId("admin-update");
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

    private static Course course(String id, String ownerUserId, CourseStop... stops) {
        return new Course(
                id,
                ownerUserId,
                id,
                "서울",
                "PUBLIC",
                "READY",
                null,
                null,
                "MD_RECOMMENDED",
                1,
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

    private static MemberRecord adminMember() {
        MemberRecord member = new MemberRecord(
                "admin",
                "관리자",
                null,
                "admin@example.com",
                "encoded-password",
                null
        );
        member.setRole("ADMIN");
        return member;
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

    private static CourseRecord courseRecord(String id, String ownerUserId) {
        return new CourseRecord(
                id,
                ownerUserId,
                id,
                "서울",
                "PUBLIC",
                "READY",
                null,
                null,
                null,
                null
        );
    }
}

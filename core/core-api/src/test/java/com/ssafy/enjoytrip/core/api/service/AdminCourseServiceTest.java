package com.ssafy.enjoytrip.core.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseRoute;
import com.ssafy.enjoytrip.core.domain.CourseReader;
import com.ssafy.enjoytrip.core.domain.CourseStop;
import com.ssafy.enjoytrip.core.domain.CourseStopPointResolver;
import com.ssafy.enjoytrip.core.domain.CourseStopTarget;
import com.ssafy.enjoytrip.core.domain.CourseWriter;
import com.ssafy.enjoytrip.core.domain.DefaultCourseRoutePlanner;
import com.ssafy.enjoytrip.core.domain.MemberRole;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemDetailRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRouteSegmentRecord;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
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

class AdminCourseServiceTest {
    private static final Long ADMIN_MEMBER_ID = 1L;

    private CourseMapper courseMapper;
    private MemberMapper memberMapper;
    private AttractionMapper attractionMapper;
    private AdminCourseService service;
    private NoteMapper noteMapper;

    @BeforeEach
    void setUp() {
        courseMapper = mock(CourseMapper.class);
        memberMapper = mock(MemberMapper.class);
        attractionMapper = mock(AttractionMapper.class);
        noteMapper = mock(NoteMapper.class);
        when(courseMapper.updateStartLocation(any(), any(), any())).thenReturn(1);
        CourseStopPointResolver stopPointResolver = new CourseStopPointResolver(attractionMapper, noteMapper);
        DefaultCourseRoutePlanner routePlanner = new DefaultCourseRoutePlanner();
        service = new AdminCourseService(
                new CourseReader(courseMapper),
                memberMapper,
                new CourseWriter(courseMapper, stopPointResolver, routePlanner)
        );
    }

    @DisplayName("관리자 코스 생성도 계획된 경로를 저장하고 구간을 재구성한다")
    @Test
    void createAdminCoursePersistsAndReadsPlannedRoute() {
        Course course = course(
                "admin-course",
                ADMIN_MEMBER_ID,
                attractionStop(10L, 1),
                attractionStop(20L, 2)
        );
        when(memberMapper.findById(ADMIN_MEMBER_ID)).thenReturn(adminMember());
        stubAttraction(10L, 37.0, 127.0);
        stubAttraction(20L, 37.1, 127.1);
        stubGeneratedItemIds(301L, 302L);
        when(courseMapper.insertSegments(any())).thenReturn(1);

        Course created = service.createAdminCourse(course);

        assertThat(created.routeSummary().segmentCount()).isEqualTo(1);
        verify(courseMapper, never()).deleteSegmentsByCourseId("admin-course");
        verify(courseMapper, never()).deleteItemsByCourseId("admin-course");
        verify(courseMapper).insertSegments(any());
    }

    @DisplayName("관리자 코스 생성은 쪽지 항목 저장 시 note_id만 채운다")
    @Test
    void createAdminCoursePersistsNoteTargetOnly() {
        Course course = course("admin-note", ADMIN_MEMBER_ID, noteStop(30L, 1));
        when(memberMapper.findById(ADMIN_MEMBER_ID)).thenReturn(adminMember());
        stubNote(30L, 37.0, 127.0);
        stubGeneratedItemIds(401L);

        service.createAdminCourse(course);

        CourseItemRecord insertedItem = firstInsertedItem();
        assertThat(insertedItem.getItemType()).isEqualTo("NOTE");
        assertThat(insertedItem.getAttractionId()).isNull();
        assertThat(insertedItem.getNoteId()).isEqualTo(30L);
    }

    @DisplayName("관리자 코스 생성은 숨김 장소를 항목으로 저장하지 않는다")
    @Test
    void createAdminCourseRejectsHiddenAttraction() {
        Course course = course("admin-hidden", ADMIN_MEMBER_ID, attractionStop(10L, 1));
        when(memberMapper.findById(ADMIN_MEMBER_ID)).thenReturn(adminMember());
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
                ADMIN_MEMBER_ID,
                attractionStop(10L, 1),
                attractionStop(20L, 2)
        );
        when(memberMapper.findById(ADMIN_MEMBER_ID)).thenReturn(adminMember());
        stubAttraction(10L, 37.0, 127.0);
        stubAttraction(20L, 37.1, 127.1);
        when(courseMapper.findById("admin-update")).thenReturn(courseRecord("admin-update", ADMIN_MEMBER_ID));
        when(courseMapper.findItemsByCourseId("admin-update")).thenReturn(List.of(
                itemDetail(501L, "admin-update", 10L, 1, "첫 장소"),
                itemDetail(502L, "admin-update", 20L, 2, "두 번째 장소")
        ));
        when(courseMapper.findSegmentsByCourseId("admin-update")).thenReturn(List.of(
                new CourseRouteSegmentRecord("admin-update", 501L, 502L, 1, "WALK", 100, 140)
        ));
        when(courseMapper.updateOwned(any(CourseRecord.class))).thenReturn(1);
        stubGeneratedItemIds(501L, 502L);
        when(courseMapper.insertSegments(any())).thenReturn(1);

        Course updated = service.updateAdminCourse(ADMIN_MEMBER_ID, course);

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

    private void stubGeneratedItemIds(Long... ids) {
        when(courseMapper.insertItems(any())).thenAnswer(invocation -> {
            List<CourseItemRecord> records = invocation.getArgument(0);
            for (int index = 0; index < records.size(); index++) {
                records.get(index).setId(ids[index]);
            }
            return records.size();
        });
    }

    @SuppressWarnings("unchecked")
    private CourseItemRecord firstInsertedItem() {
        ArgumentCaptor<List<CourseItemRecord>> itemCaptor = ArgumentCaptor.forClass(List.class);
        verify(courseMapper).insertItems(itemCaptor.capture());
        return itemCaptor.getValue().get(0);
    }

    private static Course course(String id, Long ownerMemberId, CourseStop... stops) {
        return new Course(
                id,
                ownerMemberId,
                id,
                "서울",
                "PUBLIC",
                "READY",
                null,
                null,
                "MD_RECOMMENDED",
                1,
                true,
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
                "관리자",
                null,
                "admin@example.com",
                "encoded-password",
                null
        );
        member.setId(ADMIN_MEMBER_ID);
        member.setRole(MemberRole.ADMIN.name());
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
                11L,
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

    private static CourseRecord courseRecord(String id, Long ownerMemberId) {
        CourseRecord record = new CourseRecord(
                id,
                ownerMemberId,
                id,
                "서울",
                "PUBLIC",
                "READY",
                null,
                null,
                null,
                null
        );
        record.setCreatedByAdmin(ADMIN_MEMBER_ID.equals(ownerMemberId));
        return record;
    }
}

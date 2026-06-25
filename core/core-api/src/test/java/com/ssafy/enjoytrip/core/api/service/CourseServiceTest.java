package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_ACCESS_DENIED;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVALID_ITEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.AiCourseOrderOptimizer;
import com.ssafy.enjoytrip.core.domain.CoordinateRouteOrderOptimizer;
import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseOrderOptimizationContext;
import com.ssafy.enjoytrip.core.domain.CourseOrderPreviewReader;
import com.ssafy.enjoytrip.core.domain.CourseReader;
import com.ssafy.enjoytrip.core.domain.CourseStop;
import com.ssafy.enjoytrip.core.domain.CourseStopTarget;
import com.ssafy.enjoytrip.core.domain.CourseStopPointResolver;
import com.ssafy.enjoytrip.core.domain.CourseWriter;
import com.ssafy.enjoytrip.core.domain.DefaultCourseRoutePlanner;
import com.ssafy.enjoytrip.core.domain.query.DistanceSearchCondition;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.external.courseorder.CourseOrderRecommendationException;
import com.ssafy.enjoytrip.external.courseorder.CourseOrderRecommendationRequest;
import com.ssafy.enjoytrip.external.courseorder.CourseOrderRecommendationResult;
import com.ssafy.enjoytrip.external.courseorder.SpringAiCourseOrderRecommendationClient;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemDetailRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class CourseServiceTest {
    private CourseMapper courseMapper;
    private AttractionMapper attractionMapper;
    private NoteMapper noteMapper;
    private SpringAiCourseOrderRecommendationClient courseOrderRecommendationClient;
    private CourseService service;

    @BeforeEach
    void setUp() {
        courseMapper = Mockito.mock(CourseMapper.class);
        attractionMapper = Mockito.mock(AttractionMapper.class);
        noteMapper = Mockito.mock(NoteMapper.class);
        courseOrderRecommendationClient = Mockito.mock(SpringAiCourseOrderRecommendationClient.class);
        when(courseMapper.updateStartLocation(any(), any(), any())).thenReturn(1);
        CourseStopPointResolver stopPointResolver = new CourseStopPointResolver(attractionMapper, noteMapper);
        DefaultCourseRoutePlanner routePlanner = new DefaultCourseRoutePlanner();
        service = new CourseService(
                new CourseReader(courseMapper),
                new CourseWriter(courseMapper, stopPointResolver, routePlanner),
                new AiCourseOrderOptimizer(
                        new CourseOrderPreviewReader(attractionMapper, noteMapper),
                        routePlanner,
                        new CoordinateRouteOrderOptimizer(),
                        courseOrderRecommendationClient
                )
        );
    }

    @DisplayName("코스 생성은 숨김 장소나 비공개 노트를 항목으로 저장하지 않는다")
    @Test
    void createCourseRejectsNonPublicItems() {
        Course course = course("course-1", 11L, attractionStop(10L, 1));
        when(attractionMapper.existsPublicVisibleById(10L)).thenReturn(0);

        assertThatThrownBy(() -> service.createCourse(course))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(COURSE_INVALID_ITEM));

        verify(courseMapper, never()).insert(any(CourseRecord.class));
    }

    @DisplayName("코스 생성은 항목이 없는(0개) 코스도 안전하게 생성한다")
    @Test
    void createCourseSupportsEmptyItems() {
        Course course = course("course-empty", 11L);

        Course created = service.createCourse(course);

        assertThat(created.stops()).isEmpty();
        verify(courseMapper).insert(any(CourseRecord.class));
        verify(courseMapper).updateStartLocation("course-empty", null, null);
    }



    @DisplayName("코스 생성은 장소 항목 저장 시 attraction_id만 채운다")
    @Test
    void createCoursePersistsAttractionTargetOnly() {
        Course course = course("course-attraction", 11L, attractionStop(10L, 1));
        stubAttraction(10L, 37.0, 127.0);
        stubGeneratedItemIds(101L);

        Course created = service.createCourse(course);

        assertThat(created.stops()).extracting(stop -> stop.target().id()).containsExactly(10L);
        CourseItemRecord insertedItem = firstInsertedItem();
        assertThat(insertedItem.getItemType()).isEqualTo("ATTRACTION");
        assertThat(insertedItem.getAttractionId()).isEqualTo(10L);
        assertThat(insertedItem.getNoteId()).isNull();
        verify(courseMapper).updateStartLocation("course-attraction", 127.0, 37.0);
    }

    @DisplayName("코스 생성은 쪽지 항목 저장 시 note_id만 채운다")
    @Test
    void createCoursePersistsNoteTargetOnly() {
        Course course = course("course-note", 11L, noteStop(30L, 1));
        stubNote(30L, 37.0, 127.0);
        stubGeneratedItemIds(301L);

        Course created = service.createCourse(course);

        assertThat(created.stops()).extracting(stop -> stop.target().id()).containsExactly(30L);
        CourseItemRecord insertedItem = firstInsertedItem();
        assertThat(insertedItem.getItemType()).isEqualTo("NOTE");
        assertThat(insertedItem.getAttractionId()).isNull();
        assertThat(insertedItem.getNoteId()).isEqualTo(30L);
        verify(courseMapper).updateStartLocation("course-note", 127.0, 37.0);
    }

    @DisplayName("코스 생성은 비공개 쪽지를 항목으로 저장하지 않는다")
    @Test
    void createCourseRejectsNonPublicNote() {
        Course course = course("course-private-note", 11L, noteStop(30L, 1));
        when(noteMapper.existsPublicActive(30L)).thenReturn(0);

        assertThatThrownBy(() -> service.createCourse(course))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(COURSE_INVALID_ITEM));

        verify(courseMapper, never()).insert(any(CourseRecord.class));
    }

    @DisplayName("코스 생성은 경유지 사이 next metric을 저장한다")
    @Test
    void createCoursePersistsNextMetrics() {
        Course course = course(
                "course-1",
                11L,
                attractionStop(10L, 1),
                attractionStop(20L, 2)
        );
        stubAttraction(10L, 37.0, 127.0);
        stubAttraction(20L, 37.1, 127.1);
        stubGeneratedItemIds(101L, 102L);

        Course created = service.createCourse(course);

        assertThat(created.stops()).extracting(stop -> stop.target().id()).containsExactly(10L, 20L);
        assertThat(created.segmentCount()).isEqualTo(1);
        verify(courseMapper, never()).deleteItemsByCourseId("course-1");

        ArgumentCaptor<List<CourseItemRecord>> itemCaptor = ArgumentCaptor.forClass(List.class);
        verify(courseMapper).insertItems(itemCaptor.capture());
        CourseItemRecord firstItem = itemCaptor.getValue().get(0);
        assertThat(firstItem.getDistanceToNext()).isPositive();
        assertThat(firstItem.getDurationToNext()).isPositive();
        assertThat(itemCaptor.getValue().get(1).getDistanceToNext()).isNull();
        assertThat(itemCaptor.getValue().get(1).getDurationToNext()).isNull();
    }

    @DisplayName("중복 대상 경유지도 사용자 순서대로 next metric을 저장한다")
    @Test
    void createCoursePersistsDuplicateTargetsByPosition() {
        Course course = course(
                "course-duplicate",
                11L,
                attractionStop(10L, 1),
                attractionStop(10L, 2)
        );
        stubAttraction(10L, 37.0, 127.0);
        stubGeneratedItemIds(201L, 202L);

        Course created = service.createCourse(course);

        assertThat(created.stops()).extracting(stop -> stop.target().id()).containsExactly(10L, 10L);
        assertThat(created.segmentCount()).isEqualTo(1);
    }

    @DisplayName("코스 생성은 구간 계산을 위해 좌표가 필요하다")
    @Test
    void createCourseRejectsMissingCoordinates() {
        Course course = course(
                "course-missing-coordinate",
                11L,
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

    @DisplayName("아이템 저장 후 생성 id가 없으면 코스 생성은 실패한다")
    @Test
    void createCourseRejectsMissingGeneratedItemId() {
        Course course = course(
                "course-reload-mismatch",
                11L,
                attractionStop(10L, 1),
                attractionStop(20L, 2)
        );
        stubAttraction(10L, 37.0, 127.0);
        stubAttraction(20L, 37.1, 127.1);
        when(courseMapper.insertItems(any())).thenReturn(2);

        assertThatThrownBy(() -> service.createCourse(course))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(COURSE_INVALID_ITEM));
    }

    @DisplayName("코스 수정은 사용자 순서로 항목을 교체하고 next metric을 저장한다")
    @Test
    void updateCoursePersistsUserOrderWithNextMetrics() {
        Course course = course(
                "course-1",
                11L,
                attractionStop(20L, 1),
                attractionStop(10L, 2)
        );
        stubAttraction(10L, 37.0, 127.0);
        stubAttraction(20L, 37.1, 127.1);
        when(courseMapper.findById("course-1")).thenReturn(courseRecord("course-1", 11L, 0));
        when(courseMapper.findItemsByCourseId("course-1")).thenReturn(List.of(
                itemDetail(101L, "course-1", 10L, 1, "기존 첫 장소"),
                itemDetail(102L, "course-1", 20L, 2, "기존 두 번째 장소")
        ));
        when(courseMapper.updateOwned(any(CourseRecord.class))).thenReturn(1);
        stubGeneratedItemIds(201L, 202L);

        Course updated = service.updateCourse(11L, course);

        assertThat(updated.stops()).extracting(stop -> stop.target().id()).containsExactly(20L, 10L);
        assertThat(updated.segmentCount()).isEqualTo(1);
        verify(courseMapper).updateOwned(any(CourseRecord.class));
        verify(courseMapper).deleteItemsByCourseId("course-1");
    }

    @DisplayName("AI 추천 미리보기는 아이템 id 순열로 응답하고 저장하지 않는다")
    @Test
    void recommendCourseOrderUsesAiPermutationWithoutPersistence() {
        stubFoundCourseWithTwoStops("course-1", 11L, 101L, 102L, 10L, 20L);
        when(attractionMapper.findByIds(List.of(10L, 20L))).thenReturn(List.of(
                attraction(10L, 37.0, 127.0),
                attraction(20L, 37.1, 127.1)
        ));
        when(courseOrderRecommendationClient.recommend(any())).thenReturn(
                new CourseOrderRecommendationResult(List.of(102L, 101L), "ok")
        );

        Course recommended = service.recommendCourseOrder(11L, "course-1");

        assertThat(recommended.stops()).extracting(CourseStop::id).containsExactly(102L, 101L);
        assertThat(recommended.stops()).extracting(CourseStop::position).containsExactly(1, 2);
        assertThat(recommended.segmentCount()).isEqualTo(1);
        verifyNoCourseWrites();
    }

    @DisplayName("AI 추천 요청은 프론트 현재 위치를 외부 프롬프트 요청으로 전달한다")
    @Test
    void recommendCourseOrderPassesCurrentLocationToAiRequest() {
        stubFoundCourseWithTwoStops("course-location", 11L, 101L, 102L, 10L, 20L);
        when(attractionMapper.findByIds(List.of(10L, 20L))).thenReturn(List.of(
                attraction(10L, 37.0, 127.0),
                attraction(20L, 37.1, 127.1)
        ));
        when(courseOrderRecommendationClient.recommend(any())).thenReturn(
                new CourseOrderRecommendationResult(List.of(102L, 101L), "ok")
        );

        service.recommendCourseOrder(
                11L,
                "course-location",
                new CourseOrderOptimizationContext(37.5665, 126.9780)
        );

        ArgumentCaptor<CourseOrderRecommendationRequest> requestCaptor = ArgumentCaptor.forClass(
                CourseOrderRecommendationRequest.class
        );
        verify(courseOrderRecommendationClient).recommend(requestCaptor.capture());
        assertThat(requestCaptor.getValue().currentLatitude()).isEqualTo(37.5665);
        assertThat(requestCaptor.getValue().currentLongitude()).isEqualTo(126.9780);
        verifyNoCourseWrites();
    }

    @DisplayName("AI 추천 실패는 좌표 최적화 순서로 성공 미리보기를 반환한다")
    @Test
    void recommendCourseOrderFallsBackToCoordinateOptimizer(CapturedOutput output) {
        stubFoundCourseWithThreeStops(
                "course-optimizer",
                11L,
                List.of(101L, 103L, 102L),
                List.of(10L, 30L, 20L)
        );
        when(attractionMapper.findByIds(List.of(10L, 30L, 20L))).thenReturn(List.of(
                attraction(10L, 37.0, 127.0),
                attraction(30L, 37.0, 129.0),
                attraction(20L, 37.0, 128.0)
        ));
        when(courseOrderRecommendationClient.recommend(any())).thenThrow(
                new CourseOrderRecommendationException(
                        CourseOrderRecommendationException.Reason.PROVIDER_ERROR,
                        "secret-token prompt raw provider body"
                )
        );

        Course recommended = service.recommendCourseOrder(11L, "course-optimizer");

        assertThat(recommended.stops()).extracting(CourseStop::id)
                .containsExactly(101L, 102L, 103L);
        assertThat(recommended.segmentCount()).isEqualTo(2);
        verifyNoCourseWrites();
        assertThat(output.getAll())
                .contains(
                        "reason=RECOMMENDATION_FAILED",
                        "courseId=course-optimizer",
                        "ownerMemberId=11",
                        "itemCount=3",
                        "providerException=com.ssafy.enjoytrip.external.courseorder."
                                + "CourseOrderRecommendationException"
                )
                .doesNotContain("secret-token", "prompt raw provider body");
    }

    @DisplayName("AI 추천 provider 예외 종류는 모두 좌표 최적화 fallback을 사용한다")
    @ParameterizedTest
    @EnumSource(value = CourseOrderRecommendationException.Reason.class, names = {
            "TIMEOUT",
            "BLANK_RESPONSE",
            "MALFORMED_RESPONSE"
    })
    void recommendCourseOrderFallsBackForProviderFailureReasons(
            CourseOrderRecommendationException.Reason reason
    ) {
        stubFoundCourseWithThreeStops(
                "course-provider-fallback",
                11L,
                List.of(101L, 103L, 102L),
                List.of(10L, 30L, 20L)
        );
        when(attractionMapper.findByIds(List.of(10L, 30L, 20L))).thenReturn(List.of(
                attraction(10L, 37.0, 127.0),
                attraction(30L, 37.0, 129.0),
                attraction(20L, 37.0, 128.0)
        ));
        when(courseOrderRecommendationClient.recommend(any())).thenThrow(
                new CourseOrderRecommendationException(reason, reason.name())
        );

        Course recommended = service.recommendCourseOrder(11L, "course-provider-fallback");

        assertThat(recommended.stops()).extracting(CourseStop::id)
                .containsExactly(101L, 102L, 103L);
        assertThat(recommended.segmentCount()).isEqualTo(2);
        verifyNoCourseWrites();
    }

    @DisplayName("AI가 아이템 id를 누락하면 좌표 최적화 fallback을 사용한다")
    @Test
    void recommendCourseOrderRejectsMissingAiIds() {
        stubFoundCourseWithTwoStops("course-missing-ai", 11L, 101L, 102L, 10L, 20L);
        when(attractionMapper.findByIds(List.of(10L, 20L))).thenReturn(List.of(
                attraction(10L, 37.0, 127.0),
                attraction(20L, 37.1, 127.1)
        ));
        when(courseOrderRecommendationClient.recommend(any())).thenReturn(
                new CourseOrderRecommendationResult(List.of(101L), "missing")
        );

        Course recommended = service.recommendCourseOrder(11L, "course-missing-ai");

        assertThat(recommended.stops()).extracting(CourseStop::id)
                .containsExactly(101L, 102L);
        verifyNoCourseWrites();
    }

    @DisplayName("AI가 없는 아이템 id를 반환하면 좌표 최적화 fallback을 사용한다")
    @Test
    void recommendCourseOrderRejectsHallucinatedAiIds() {
        stubFoundCourseWithTwoStops("course-hallucinated-ai", 11L, 101L, 102L, 10L, 20L);
        when(attractionMapper.findByIds(List.of(10L, 20L))).thenReturn(List.of(
                attraction(10L, 37.0, 127.0),
                attraction(20L, 37.1, 127.1)
        ));
        when(courseOrderRecommendationClient.recommend(any())).thenReturn(
                new CourseOrderRecommendationResult(List.of(101L, 999L), "hallucinated")
        );

        Course recommended = service.recommendCourseOrder(11L, "course-hallucinated-ai");

        assertThat(recommended.stops()).extracting(CourseStop::id)
                .containsExactly(101L, 102L);
        verifyNoCourseWrites();
    }

    @DisplayName("추천은 소유자를 확인한 뒤에만 미리보기 hydrator와 AI를 호출한다")
    @Test
    void recommendCourseOrderRejectsNonOwnerBeforePreviewAndAi() {
        stubFoundCourseWithTwoStops("course-owner-only", 22L, 101L, 102L, 10L, 20L);

        assertThatThrownBy(() -> service.recommendCourseOrder(33L, "course-owner-only"))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(COURSE_ACCESS_DENIED));

        verify(attractionMapper, never()).findByIds(any());
        verify(courseOrderRecommendationClient, never()).recommend(any());
        verifyNoCourseWrites();
    }

    @DisplayName("좌표가 부족한 코스 추천은 AI 호출 없이 현재 순서를 반환한다")
    @Test
    void recommendCourseOrderReturnsCurrentPreviewWhenCoordinatesAreInsufficient(CapturedOutput output) {
        stubFoundCourseWithTwoStops("course-missing-coordinate", 11L, 101L, 102L, 10L, 20L);
        when(attractionMapper.findByIds(List.of(10L, 20L))).thenReturn(List.of(
                attraction(10L, 37.0, 127.0),
                attraction(20L, null, 127.1)
        ));

        Course recommended = service.recommendCourseOrder(11L, "course-missing-coordinate");

        assertThat(recommended.stops()).extracting(CourseStop::id)
                .containsExactly(101L, 102L);
        assertThat(recommended.segmentCount()).isEqualTo(1);
        verify(courseOrderRecommendationClient, never()).recommend(any());
        verifyNoCourseWrites();
        assertThat(output.getAll())
                .contains(
                        "reason=COORDINATE_INSUFFICIENT",
                        "courseId=course-missing-coordinate",
                        "ownerMemberId=11",
                        "itemCount=2",
                        "providerException="
                );
    }

    @DisplayName("AI가 중복 아이템 id를 반환하면 좌표 최적화 fallback을 사용한다")
    @Test
    void recommendCourseOrderRejectsDuplicateAiIds(CapturedOutput output) {
        stubFoundCourseWithTwoStops("course-duplicate-ai", 11L, 101L, 102L, 10L, 20L);
        when(attractionMapper.findByIds(List.of(10L, 20L))).thenReturn(List.of(
                attraction(10L, 37.0, 127.0),
                attraction(20L, 37.1, 127.1)
        ));
        when(courseOrderRecommendationClient.recommend(any())).thenReturn(
                new CourseOrderRecommendationResult(List.of(101L, 101L), "duplicate")
        );

        Course recommended = service.recommendCourseOrder(11L, "course-duplicate-ai");

        assertThat(recommended.stops()).extracting(CourseStop::id)
                .containsExactly(101L, 102L);
        verifyNoCourseWrites();
        assertThat(output.getAll())
                .contains(
                        "reason=RECOMMENDATION_FAILED",
                        "courseId=course-duplicate-ai",
                        "ownerMemberId=11",
                        "itemCount=2",
                        "providerException="
                );
    }

    @DisplayName("공개 피드는 저장소 거리순 단일 목록을 반환한다")
    @Test
    void publicFeedReturnsDistanceOrderedCourses() {
        CourseRecord mdCourse = courseRecord("md-1", 1L, 0);
        mdCourse.setStartLatitude(37.5665);
        mdCourse.setStartLongitude(126.9780);
        mdCourse.setDistanceMeters(42.5);
        CourseRecord publicCourse = courseRecord("course-1", 11L, 3);
        publicCourse.setStartLatitude(37.5666);
        publicCourse.setStartLongitude(126.9781);
        publicCourse.setDistanceMeters(128.3);
        when(courseMapper.findDistanceOrderedPublicFeed(126.9780, 37.5665, 20, null)).thenReturn(List.of(
                mdCourse,
                publicCourse
        ));
        when(courseMapper.findPublicItemsByCourseId(eq("md-1"))).thenReturn(List.of());
        when(courseMapper.findPublicItemsByCourseId(eq("course-1"))).thenReturn(List.of());

        List<Course> feed = service.findPublicFeed(new DistanceSearchCondition(126.9780, 37.5665, 20, null));

        assertThat(feed).extracting(Course::id).containsExactly("md-1", "course-1");
        assertThat(feed).extracting(Course::distanceMeters).containsExactly(42.5, 128.3);
        assertThat(feed).extracting(c -> c.startLocation() != null ? c.startLocation().latitude() : null)
                .containsExactly(37.5665, 37.5666);
        assertThat(feed).extracting(c -> c.startLocation() != null ? c.startLocation().longitude() : null)
                .containsExactly(126.9780, 126.9781);
    }

    private void verifyNoCourseWrites() {
        verify(courseMapper, never()).insert(any(CourseRecord.class));
        verify(courseMapper, never()).updateOwned(any(CourseRecord.class));
        verify(courseMapper, never()).updateStartLocation(any(), any(), any());
        verify(courseMapper, never()).deleteItemsByCourseId(any(String.class));
        verify(courseMapper, never()).insertItems(any());
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

    private void stubFoundCourseWithTwoStops(String courseId,
                                             Long ownerMemberId,
                                             Long firstItemId,
                                             Long secondItemId,
                                             Long firstAttractionId,
                                             Long secondAttractionId) {
        when(courseMapper.findById(courseId)).thenReturn(courseRecord(courseId, ownerMemberId, 0));
        when(courseMapper.findItemsByCourseId(courseId)).thenReturn(List.of(
                itemDetail(firstItemId, courseId, firstAttractionId, 1, "첫 장소"),
                itemDetail(secondItemId, courseId, secondAttractionId, 2, "두 번째 장소")
        ));
    }

    private void stubFoundCourseWithThreeStops(String courseId,
                                               Long ownerMemberId,
                                               List<Long> itemIds,
                                               List<Long> attractionIds) {
        when(courseMapper.findById(courseId)).thenReturn(courseRecord(courseId, ownerMemberId, 0));
        when(courseMapper.findItemsByCourseId(courseId)).thenReturn(List.of(
                itemDetail(itemIds.get(0), courseId, attractionIds.get(0), 1, "첫 장소"),
                itemDetail(itemIds.get(1), courseId, attractionIds.get(1), 2, "두 번째 장소"),
                itemDetail(itemIds.get(2), courseId, attractionIds.get(2), 3, "세 번째 장소")
        ));
    }

    private static Course course(String id, Long ownerMemberId, CourseStop... stops) {
        return new Course(
                id,
                ownerMemberId,
                id,
                "서울",
                null,
                null,
                null,
                0,
                "",
                "",
                List.of(stops),
                List.of()
        );
    }

    private static CourseStop attractionStop(Long attractionId, int position) {
        return new CourseStop(null, CourseStopTarget.attraction(attractionId), position,
                null, null, null);
    }

    private static CourseStop noteStop(Long noteId, int position) {
        return new CourseStop(null, CourseStopTarget.note(noteId), position,
                null, null, null);
    }

    private static AttractionRecord attraction(Long id, Double latitude, Double longitude) {
        return new AttractionRecord(
                id, "장소 " + id, null, null, null, null, null, null, 0, null, null,
                latitude, longitude, null, null, null
        );
    }

    private static NoteRecord note(Long id, Double latitude, Double longitude) {
        return new NoteRecord(
                id, 11L, "쪽지 " + id, "내용", "TIP", "PUBLIC",
                bigDecimal(latitude), bigDecimal(longitude), "서울", null, null, null
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
                id, courseId, "ATTRACTION", attractionId, null, position,
                null, null, title, null, title, null, null
        );
    }

    private static CourseRecord courseRecord(String id, Long ownerMemberId, Integer saveCount) {
        CourseRecord record = new CourseRecord(id, ownerMemberId, id, "서울", null);
        record.setSaveCount(saveCount);
        return record;
    }
}

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

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseRoute;
import com.ssafy.enjoytrip.core.domain.CourseStop;
import com.ssafy.enjoytrip.core.domain.CourseStopTarget;
import com.ssafy.enjoytrip.core.domain.AiCourseOrderOptimizer;
import com.ssafy.enjoytrip.core.domain.CoordinateRouteOrderOptimizer;
import com.ssafy.enjoytrip.core.domain.CourseFeedSection;
import com.ssafy.enjoytrip.core.domain.CourseOrderOptimizationContext;
import com.ssafy.enjoytrip.core.domain.CourseOrderPreviewReader;
import com.ssafy.enjoytrip.core.domain.CourseReader;
import com.ssafy.enjoytrip.core.domain.CourseStopPointResolver;
import com.ssafy.enjoytrip.core.domain.CourseWriter;
import com.ssafy.enjoytrip.core.domain.DefaultCourseRoutePlanner;
import com.ssafy.enjoytrip.external.courseorder.CourseOrderRecommendationException;
import com.ssafy.enjoytrip.external.courseorder.CourseOrderRecommendationRequest;
import com.ssafy.enjoytrip.external.courseorder.CourseOrderRecommendationResult;
import com.ssafy.enjoytrip.external.courseorder.SpringAiCourseOrderRecommendationClient;
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
        stubGeneratedItemIds(101L);

        Course created = service.createCourse(course);

        assertThat(created.route().stops()).extracting(stop -> stop.target().id()).containsExactly(10L);
        CourseItemRecord insertedItem = firstInsertedItem();
        assertThat(insertedItem.getItemType()).isEqualTo("ATTRACTION");
        assertThat(insertedItem.getAttractionId()).isEqualTo(10L);
        assertThat(insertedItem.getNoteId()).isNull();
    }

    @DisplayName("코스 생성은 쪽지 항목 저장 시 note_id만 채운다")
    @Test
    void createCoursePersistsNoteTargetOnly() {
        Course course = course("course-note", "user", "PRIVATE", "READY", noteStop(30L, 1));
        stubNote(30L, 37.0, 127.0);
        stubGeneratedItemIds(301L);

        Course created = service.createCourse(course);

        assertThat(created.route().stops()).extracting(stop -> stop.target().id()).containsExactly(30L);
        CourseItemRecord insertedItem = firstInsertedItem();
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

    @DisplayName("코스 생성은 사용자 순서대로 인접 구간을 저장한다")
    @Test
    void createCoursePersistsUserOrderWithSegments() {
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
        stubGeneratedItemIds(101L, 102L);
        when(courseMapper.insertSegments(any())).thenReturn(1);

        Course created = service.createCourse(course);

        assertThat(created.route().stops()).extracting(stop -> stop.target().id()).containsExactly(10L, 20L);
        assertThat(created.routeSummary().segmentCount()).isEqualTo(1);
        verify(courseMapper, never()).deleteSegmentsByCourseId("course-1");
        verify(courseMapper, never()).deleteItemsByCourseId("course-1");
        verify(courseMapper).insertSegments(any());
    }

    @DisplayName("중복 대상 경유지도 사용자 순서대로 구간을 저장한다")
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
        stubGeneratedItemIds(201L, 202L);
        when(courseMapper.insertSegments(any())).thenReturn(1);

        Course created = service.createCourse(course);

        assertThat(created.route().stops()).extracting(stop -> stop.target().id()).containsExactly(10L, 10L);
        assertThat(created.routeSummary().segmentCount()).isEqualTo(1);
        verify(courseMapper).insertSegments(any());
    }

    @DisplayName("코스 생성은 구간 계산을 위해 좌표가 필요하다")
    @Test
    void createCourseRejectsMissingCoordinates() {
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
        verify(courseMapper, never()).insertSegments(any());
    }

    @DisplayName("아이템 저장 후 생성 id가 없으면 코스 생성은 실패한다")
    @Test
    void createCourseRejectsMissingGeneratedItemId() {
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
        when(courseMapper.insertItems(any())).thenReturn(2);

        assertThatThrownBy(() -> service.createCourse(course))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(COURSE_INVALID_ITEM));

        verify(courseMapper, never()).insertSegments(any());
    }



    @DisplayName("코스 수정은 사용자 순서로 항목과 구간을 교체한다")
    @Test
    void updateCoursePersistsUserOrderWithSegments() {
        Course course = course(
                "course-1",
                "user",
                "PRIVATE",
                "READY",
                attractionStop(20L, 1),
                attractionStop(10L, 2)
        );
        stubAttraction(10L, 37.0, 127.0);
        stubAttraction(20L, 37.1, 127.1);
        when(courseMapper.findById("course-1")).thenReturn(courseRecord("course-1", "user", null, null, 0));
        when(courseMapper.findItemsByCourseId("course-1")).thenReturn(List.of(
                itemDetail(101L, "course-1", 10L, 1, "기존 첫 장소"),
                itemDetail(102L, "course-1", 20L, 2, "기존 두 번째 장소")
        ));
        when(courseMapper.findSegmentsByCourseId("course-1")).thenReturn(List.of(
                new CourseRouteSegmentRecord("course-1", 101L, 102L, 1, "WALK", 100, 140)
        ));
        when(courseMapper.updateOwned(any(CourseRecord.class))).thenReturn(1);
        stubGeneratedItemIds(201L, 202L);
        when(courseMapper.insertSegments(any())).thenReturn(1);

        Course updated = service.updateCourse("user", course);

        assertThat(updated.route().stops()).extracting(stop -> stop.target().id()).containsExactly(20L, 10L);
        assertThat(updated.routeSummary().segmentCount()).isEqualTo(1);
        verify(courseMapper).updateOwned(any(CourseRecord.class));
        verify(courseMapper).deleteSegmentsByCourseId("course-1");
        verify(courseMapper).deleteItemsByCourseId("course-1");
        verify(courseMapper).insertSegments(any());
    }

    @DisplayName("AI 추천 미리보기는 아이템 id 순열로 응답하고 저장하지 않는다")
    @Test
    void recommendCourseOrderUsesAiPermutationWithoutPersistence() {
        stubFoundCourseWithSegment("course-1", "user", 101L, 102L, 10L, 20L);
        when(attractionMapper.findByIds(List.of(10L, 20L))).thenReturn(List.of(
                attraction(10L, 37.0, 127.0),
                attraction(20L, 37.1, 127.1)
        ));
        when(courseOrderRecommendationClient.recommend(any())).thenReturn(
                new CourseOrderRecommendationResult(List.of(102L, 101L), "ok")
        );

        Course recommended = service.recommendCourseOrder("user", "course-1");

        assertThat(recommended.route().stops()).extracting(CourseStop::id).containsExactly(102L, 101L);
        assertThat(recommended.route().stops()).extracting(CourseStop::position).containsExactly(1, 2);
        assertThat(recommended.routeSummary().segmentCount()).isEqualTo(1);
        verifyNoCourseWrites();
    }

    @DisplayName("AI 추천 요청은 프론트 현재 위치를 외부 프롬프트 요청으로 전달한다")
    @Test
    void recommendCourseOrderPassesCurrentLocationToAiRequest() {
        stubFoundCourseWithSegment("course-location", "user", 101L, 102L, 10L, 20L);
        when(attractionMapper.findByIds(List.of(10L, 20L))).thenReturn(List.of(
                attraction(10L, 37.0, 127.0),
                attraction(20L, 37.1, 127.1)
        ));
        when(courseOrderRecommendationClient.recommend(any())).thenReturn(
                new CourseOrderRecommendationResult(List.of(102L, 101L), "ok")
        );

        service.recommendCourseOrder(
                "user",
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
                "user",
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

        Course recommended = service.recommendCourseOrder("user", "course-optimizer");

        assertThat(recommended.route().stops()).extracting(CourseStop::id)
                .containsExactly(101L, 102L, 103L);
        assertThat(recommended.routeSummary().segmentCount()).isEqualTo(2);
        verifyNoCourseWrites();
        assertThat(output.getAll())
                .contains(
                        "reason=RECOMMENDATION_FAILED",
                        "courseId=course-optimizer",
                        "ownerUserId=user",
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
                "user",
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

        Course recommended = service.recommendCourseOrder("user", "course-provider-fallback");

        assertThat(recommended.route().stops()).extracting(CourseStop::id)
                .containsExactly(101L, 102L, 103L);
        assertThat(recommended.routeSummary().segmentCount()).isEqualTo(2);
        verifyNoCourseWrites();
    }

    @DisplayName("AI가 아이템 id를 누락하면 좌표 최적화 fallback을 사용한다")
    @Test
    void recommendCourseOrderRejectsMissingAiIds() {
        stubFoundCourseWithSegment("course-missing-ai", "user", 101L, 102L, 10L, 20L);
        when(attractionMapper.findByIds(List.of(10L, 20L))).thenReturn(List.of(
                attraction(10L, 37.0, 127.0),
                attraction(20L, 37.1, 127.1)
        ));
        when(courseOrderRecommendationClient.recommend(any())).thenReturn(
                new CourseOrderRecommendationResult(List.of(101L), "missing")
        );

        Course recommended = service.recommendCourseOrder("user", "course-missing-ai");

        assertThat(recommended.route().stops()).extracting(CourseStop::id)
                .containsExactly(101L, 102L);
        verifyNoCourseWrites();
    }

    @DisplayName("AI가 없는 아이템 id를 반환하면 좌표 최적화 fallback을 사용한다")
    @Test
    void recommendCourseOrderRejectsHallucinatedAiIds() {
        stubFoundCourseWithSegment("course-hallucinated-ai", "user", 101L, 102L, 10L, 20L);
        when(attractionMapper.findByIds(List.of(10L, 20L))).thenReturn(List.of(
                attraction(10L, 37.0, 127.0),
                attraction(20L, 37.1, 127.1)
        ));
        when(courseOrderRecommendationClient.recommend(any())).thenReturn(
                new CourseOrderRecommendationResult(List.of(101L, 999L), "hallucinated")
        );

        Course recommended = service.recommendCourseOrder("user", "course-hallucinated-ai");

        assertThat(recommended.route().stops()).extracting(CourseStop::id)
                .containsExactly(101L, 102L);
        verifyNoCourseWrites();
    }

    @DisplayName("추천은 소유자를 확인한 뒤에만 미리보기 hydrator와 AI를 호출한다")
    @Test
    void recommendCourseOrderRejectsNonOwnerBeforePreviewAndAi() {
        stubFoundCourseWithSegment("course-owner-only", "owner", 101L, 102L, 10L, 20L);

        assertThatThrownBy(() -> service.recommendCourseOrder("intruder", "course-owner-only"))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(COURSE_ACCESS_DENIED));

        verify(attractionMapper, never()).findByIds(any());
        verify(courseOrderRecommendationClient, never()).recommend(any());
        verifyNoCourseWrites();
    }

    @DisplayName("좌표가 부족한 코스 추천은 AI 호출 없이 현재 순서를 반환한다")
    @Test
    void recommendCourseOrderReturnsCurrentPreviewWhenCoordinatesAreInsufficient(CapturedOutput output) {
        stubFoundCourseWithSegment("course-missing-coordinate", "user", 101L, 102L, 10L, 20L);
        when(attractionMapper.findByIds(List.of(10L, 20L))).thenReturn(List.of(
                attraction(10L, 37.0, 127.0),
                attraction(20L, null, 127.1)
        ));

        Course recommended = service.recommendCourseOrder("user", "course-missing-coordinate");

        assertThat(recommended.route().stops()).extracting(CourseStop::id)
                .containsExactly(101L, 102L);
        assertThat(recommended.routeSummary().segmentCount()).isEqualTo(1);
        verify(courseOrderRecommendationClient, never()).recommend(any());
        verifyNoCourseWrites();
        assertThat(output.getAll())
                .contains(
                        "reason=COORDINATE_INSUFFICIENT",
                        "courseId=course-missing-coordinate",
                        "ownerUserId=user",
                        "itemCount=2",
                        "providerException="
                );
    }

    @DisplayName("AI가 중복 아이템 id를 반환하면 좌표 최적화 fallback을 사용한다")
    @Test
    void recommendCourseOrderRejectsDuplicateAiIds(CapturedOutput output) {
        stubFoundCourseWithSegment("course-duplicate-ai", "user", 101L, 102L, 10L, 20L);
        when(attractionMapper.findByIds(List.of(10L, 20L))).thenReturn(List.of(
                attraction(10L, 37.0, 127.0),
                attraction(20L, 37.1, 127.1)
        ));
        when(courseOrderRecommendationClient.recommend(any())).thenReturn(
                new CourseOrderRecommendationResult(List.of(101L, 101L), "duplicate")
        );

        Course recommended = service.recommendCourseOrder("user", "course-duplicate-ai");

        assertThat(recommended.route().stops()).extracting(CourseStop::id)
                .containsExactly(101L, 102L);
        verifyNoCourseWrites();
        assertThat(output.getAll())
                .contains(
                        "reason=RECOMMENDATION_FAILED",
                        "courseId=course-duplicate-ai",
                        "ownerUserId=user",
                        "itemCount=2",
                        "providerException="
                );
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
        assertThat(feed.get(0).courses()).extracting(Course::createdByAdmin).containsExactly(true);
        assertThat(feed.get(1).key()).isEqualTo("POPULAR");
        assertThat(feed.get(1).courses()).extracting(Course::saveCount).containsExactly(3);
        assertThat(feed.get(1).courses()).extracting(Course::createdByAdmin).containsExactly(true);
    }

    private void verifyNoCourseWrites() {
        verify(courseMapper, never()).insert(any(CourseRecord.class));
        verify(courseMapper, never()).updateOwned(any(CourseRecord.class));
        verify(courseMapper, never()).deleteSegmentsByCourseId(any(String.class));
        verify(courseMapper, never()).deleteItemsByCourseId(any(String.class));
        verify(courseMapper, never()).insertItems(any());
        verify(courseMapper, never()).insertSegments(any());
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

    private void stubFoundCourseWithThreeStops(String courseId,
                                               String ownerUserId,
                                               List<Long> itemIds,
                                               List<Long> attractionIds) {
        when(courseMapper.findById(courseId)).thenReturn(courseRecord(courseId, ownerUserId, null, null, 0));
        when(courseMapper.findItemsByCourseId(courseId)).thenReturn(List.of(
                itemDetail(itemIds.get(0), courseId, attractionIds.get(0), 1, "첫 장소"),
                itemDetail(itemIds.get(1), courseId, attractionIds.get(1), 2, "두 번째 장소"),
                itemDetail(itemIds.get(2), courseId, attractionIds.get(2), 3, "세 번째 장소")
        ));
        when(courseMapper.findSegmentsByCourseId(courseId)).thenReturn(List.of(
                new CourseRouteSegmentRecord(courseId, itemIds.get(0), itemIds.get(1), 1, "WALK", 100, 140),
                new CourseRouteSegmentRecord(courseId, itemIds.get(1), itemIds.get(2), 2, "WALK", 100, 140)
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
                false,
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
        record.setCreatedByAdmin("admin".equals(ownerUserId));
        return record;
    }
}

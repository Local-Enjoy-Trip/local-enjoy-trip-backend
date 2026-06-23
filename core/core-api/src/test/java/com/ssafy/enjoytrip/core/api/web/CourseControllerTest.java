package com.ssafy.enjoytrip.core.api.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ssafy.enjoytrip.core.api.web.controller.CourseController;
import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseRoute;
import com.ssafy.enjoytrip.core.domain.CourseRouteSegment;
import com.ssafy.enjoytrip.core.domain.CourseStop;
import com.ssafy.enjoytrip.core.domain.CourseStopTarget;
import com.ssafy.enjoytrip.core.domain.CourseOrderOptimizationContext;
import com.ssafy.enjoytrip.core.domain.query.DistanceSearchCondition;
import com.ssafy.enjoytrip.core.domain.service.CourseService;
import java.security.Principal;
import java.util.List;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CourseControllerTest {
    private CourseService courseService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        courseService = mock(CourseService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CourseController(courseService))
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @DisplayName("공개 코스 피드는 단일 목록과 시작 지점 거리로 반환한다")
    @Test
    void returnsPublicCourseFeedCourses() throws Exception {
        when(courseService.findPublicFeed(any())).thenReturn(List.of(
                feedCourse("md-1", "admin", "MD_RECOMMENDED", 42.5),
                feedCourse("course-1", "ssafy", null, 128.3)
        ));

        mockMvc.perform(get("/api/courses/feed")
                        .param("mapX", "126.9780")
                        .param("mapY", "37.5665")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sections").doesNotExist())
                .andExpect(jsonPath("$.data.courses[0].id").value("md-1"))
                .andExpect(jsonPath("$.data.courses[0].curationSection").value("MD_RECOMMENDED"))
                .andExpect(jsonPath("$.data.courses[0].createdByAdmin").value(true))
                .andExpect(jsonPath("$.data.courses[0].distanceMeters").value(42.5))
                .andExpect(jsonPath("$.data.courses[0].startLocation.longitude").value(126.978))
                .andExpect(jsonPath("$.data.courses[0].startLocation.latitude").value(37.5665))
                .andExpect(jsonPath("$.data.courses[0].routeSummary.stopCount").value(2))
                .andExpect(jsonPath("$.data.courses[0].segments[0].distanceMeters").value(140))
                .andExpect(jsonPath("$.data.courses[0].encodedPolyline").doesNotExist())
                .andExpect(jsonPath("$.data.courses[1].id").value("course-1"));

        ArgumentCaptor<DistanceSearchCondition> conditionCaptor = ArgumentCaptor.forClass(DistanceSearchCondition.class);
        verify(courseService).findPublicFeed(conditionCaptor.capture());
        assertThat(conditionCaptor.getValue().longitude()).isEqualTo(126.9780);
        assertThat(conditionCaptor.getValue().latitude()).isEqualTo(37.5665);
        assertThat(conditionCaptor.getValue().limit()).isEqualTo(20);
        assertThat(conditionCaptor.getValue().radiusMeters()).isNull();
    }

    @DisplayName("공개 코스 피드는 좌표를 필수로 검증한다")
    @Test
    void rejectsPublicCourseFeedWithoutCoordinates() throws Exception {
        mockMvc.perform(get("/api/courses/feed")
                        .param("limit", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(courseService, never()).findPublicFeed(any());
    }

    @DisplayName("공개 코스 피드는 limit과 radius 범위를 검증한다")
    @Test
    void rejectsPublicCourseFeedInvalidLimitAndRadius() throws Exception {
        mockMvc.perform(get("/api/courses/feed")
                        .param("mapX", "126.9780")
                        .param("mapY", "37.5665")
                        .param("limit", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/courses/feed")
                        .param("mapX", "126.9780")
                        .param("mapY", "37.5665")
                        .param("radius", "20000.1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(courseService, never()).findPublicFeed(any());
    }

    @DisplayName("공개 코스 상세는 경로 요약과 items와 segments를 반환한다")
    @Test
    void returnsPublicCourseDetail() throws Exception {
        when(courseService.findPublicRequired("course-1")).thenReturn(
                course("course-1", "admin", "PUBLIC", "READY", 0)
        );

        mockMvc.perform(get("/api/courses/course-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("course-1"))
                .andExpect(jsonPath("$.data.createdByAdmin").value(true))
                .andExpect(jsonPath("$.data.routeSummary.stopCount").value(2))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.segments[0].fromPosition").value(1))
                .andExpect(jsonPath("$.data.segments[0].toPosition").value(2))
                .andExpect(jsonPath("$.data.encodedPolyline").doesNotExist());
    }

    @DisplayName("인증 사용자는 본인 코스 목록에서 경로 요약을 확인한다")
    @Test
    void returnsMyCoursesWithRouteSummary() throws Exception {
        when(courseService.findMyCourses("ssafy")).thenReturn(List.of(
                course("course-1", "ssafy", "PRIVATE", "READY", 0)
        ));

        mockMvc.perform(get("/api/courses/me").principal(jwtPrincipal("ssafy")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.courses[0].id").value("course-1"))
                .andExpect(jsonPath("$.data.courses[0].routeSummary.stopCount").value(2))
                .andExpect(jsonPath("$.data.courses[0].items").isArray())
                .andExpect(jsonPath("$.data.courses[0].segments").isArray())
                .andExpect(jsonPath("$.data.courses[0].encodedPolyline").doesNotExist());
    }

    @DisplayName("인증 사용자는 JSON 요청으로 READY 상태의 본인 코스를 생성한다")
    @Test
    void authenticatedUserCreatesCourse() throws Exception {
        when(courseService.createCourse(any())).thenReturn(
                course("course-1", "ssafy", "PRIVATE", "READY", 0)
        );

        mockMvc.perform(post("/api/courses")
                        .principal(jwtPrincipal("ssafy"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id":"course-1",
                                  "title":"서울 산책",
                                  "visibility":"PRIVATE",
                                  "status":"READY",
                                  "items":[
                                    {"itemType":"ATTRACTION","attractionId":1,"position":1},
                                    {"itemType":"ATTRACTION","attractionId":2,"position":2}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("course-1"))
                .andExpect(jsonPath("$.data.ownerUserId").value("ssafy"))
                .andExpect(jsonPath("$.data.routeSummary.stopCount").value(2))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.segments[0].distanceMeters").value(140))
                .andExpect(jsonPath("$.data.encodedPolyline").doesNotExist());

        verify(courseService).createCourse(any());
    }

    @DisplayName("코스 생성 요청은 최소 2개 항목이 필요하다")
    @Test
    void createCourseRejectsSingleItem() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .principal(jwtPrincipal("ssafy"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id":"course-single-item",
                                  "title":"서울 산책",
                                  "visibility":"PRIVATE",
                                  "status":"READY",
                                  "items":[
                                    {"itemType":"ATTRACTION","attractionId":1}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(courseService, never()).createCourse(any());
    }

    @DisplayName("코스 생성 요청은 position 값이 아니라 items 배열 순서를 사용한다")
    @Test
    void createCourseUsesItemArrayOrderIgnoringPositions() throws Exception {
        when(courseService.createCourse(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/courses")
                        .principal(jwtPrincipal("ssafy"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id":"course-array-order",
                                  "title":"서울 산책",
                                  "visibility":"PRIVATE",
                                  "status":"READY",
                                  "items":[
                                    {"itemType":"ATTRACTION","attractionId":2,"position":99},
                                    {"itemType":"ATTRACTION","attractionId":1,"position":-10}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseService).createCourse(courseCaptor.capture());
        assertThat(courseCaptor.getValue().route().stops()).extracting(stop -> stop.target().id())
                .containsExactly(2L, 1L);
        assertThat(courseCaptor.getValue().route().stops()).extracting(CourseStop::position)
                .containsExactly(1, 2);
    }

    @DisplayName("인증 사용자는 본인 코스를 수정하고 경로 요약 응답을 받는다")
    @Test
    void authenticatedUserUpdatesCourse() throws Exception {
        when(courseService.updateCourse(eq("ssafy"), any())).thenReturn(
                course("course-1", "ssafy", "PRIVATE", "READY", 0)
        );

        mockMvc.perform(put("/api/courses/course-1")
                        .principal(jwtPrincipal("ssafy"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"서울 산책 수정",
                                  "visibility":"PRIVATE",
                                  "items":[
                                    {"itemType":"ATTRACTION","attractionId":1,"position":1},
                                    {"itemType":"ATTRACTION","attractionId":2,"position":2}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("course-1"))
                .andExpect(jsonPath("$.data.routeSummary.stopCount").value(2))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.segments").isArray())
                .andExpect(jsonPath("$.data.encodedPolyline").doesNotExist());

        verify(courseService).updateCourse(eq("ssafy"), any());
    }

    @DisplayName("코스 수정 요청은 position 값이 아니라 items 배열 순서를 사용한다")
    @Test
    void updateCourseUsesItemArrayOrderIgnoringPositions() throws Exception {
        when(courseService.updateCourse(eq("ssafy"), any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        mockMvc.perform(put("/api/courses/course-1")
                        .principal(jwtPrincipal("ssafy"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"서울 산책 수정",
                                  "visibility":"PRIVATE",
                                  "items":[
                                    {"itemType":"ATTRACTION","attractionId":2,"position":99},
                                    {"itemType":"ATTRACTION","attractionId":1,"position":-10}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseService).updateCourse(eq("ssafy"), courseCaptor.capture());
        assertThat(courseCaptor.getValue().route().stops()).extracting(stop -> stop.target().id())
                .containsExactly(2L, 1L);
        assertThat(courseCaptor.getValue().route().stops()).extracting(CourseStop::position)
                .containsExactly(1, 2);
    }

    @DisplayName("코스 순서 추천 미리보기는 저장된 아이템 id를 반환한다")
    @Test
    void recommendCourseOrderReturnsCourseResponseShape() throws Exception {
        when(courseService.recommendCourseOrder(eq("ssafy"), eq("course-1"), any())).thenReturn(
                courseWithStoredStops("course-1", "ssafy", 101L, 102L)
        );

        mockMvc.perform(post("/api/courses/course-1/order-recommendation")
                        .principal(jwtPrincipal("ssafy"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentLatitude": 37.5665,
                                  "currentLongitude": 126.9780
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("course-1"))
                .andExpect(jsonPath("$.data.items[0].id").value(101L))
                .andExpect(jsonPath("$.data.items[0].position").value(1))
                .andExpect(jsonPath("$.data.routeSummary.stopCount").value(2))
                .andExpect(jsonPath("$.data.segments").isArray())
                .andExpect(jsonPath("$.data.recommendationSource").doesNotExist())
                .andExpect(jsonPath("$.data.fallbackReason").doesNotExist())
                .andExpect(jsonPath("$.data.provider").doesNotExist());

        ArgumentCaptor<CourseOrderOptimizationContext> contextCaptor = ArgumentCaptor.forClass(
                CourseOrderOptimizationContext.class
        );
        verify(courseService).recommendCourseOrder(eq("ssafy"), eq("course-1"), contextCaptor.capture());
        assertThat(contextCaptor.getValue().currentLatitude()).isEqualTo(37.5665);
        assertThat(contextCaptor.getValue().currentLongitude()).isEqualTo(126.9780);
    }

    @DisplayName("코스 생성 요청은 DRAFT 상태를 거부한다")
    @Test
    void rejectsDraftStatus() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .principal(jwtPrincipal("ssafy"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id":"course-1",
                                  "title":"서울 산책",
                                  "visibility":"PRIVATE",
                                  "status":"DRAFT",
                                  "items":[
                                    {"itemType":"ATTRACTION","attractionId":1},
                                    {"itemType":"ATTRACTION","attractionId":2}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(courseService, never()).createCourse(any());
    }

    private static Course course(String id,
                                 String ownerUserId,
                                 String visibility,
                                 String status,
                                 int saveCount) {
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
                "admin".equals(ownerUserId),
                saveCount,
                "",
                "",
                CourseRoute.planned(
                        List.of(attractionStop(null, 1L, 1), attractionStop(null, 2L, 2)),
                        List.of(new CourseRouteSegment(1, 1, 2, "WALK", 100, 140))
                )
        );
    }

    private static Course feedCourse(String id,
                                     String ownerUserId,
                                     String curationSection,
                                     Double distanceMeters) {
        return new Course(
                id,
                ownerUserId,
                id,
                "서울",
                "PUBLIC",
                "READY",
                null,
                null,
                curationSection,
                null,
                "admin".equals(ownerUserId),
                37.5665,
                126.9780,
                distanceMeters,
                0,
                "",
                "",
                CourseRoute.planned(
                        List.of(attractionStop(null, 1L, 1), attractionStop(null, 2L, 2)),
                        List.of(new CourseRouteSegment(1, 1, 2, "WALK", 100, 140))
                )
        );
    }

    private static Course courseWithStoredStops(String id,
                                                String ownerUserId,
                                                Long firstItemId,
                                                Long secondItemId) {
        return new Course(
                id,
                ownerUserId,
                id,
                "서울",
                "PRIVATE",
                "READY",
                null,
                null,
                null,
                null,
                false,
                0,
                "",
                "",
                CourseRoute.planned(
                        List.of(
                                attractionStop(firstItemId, 1L, 1),
                                attractionStop(secondItemId, 2L, 2)
                        ),
                        List.of(new CourseRouteSegment(1, 1, 2, "WALK", 100, 140))
                )
        );
    }

    private static CourseStop attractionStop(Long attractionId) {
        return new CourseStop(
                null,
                CourseStopTarget.attraction(attractionId),
                1,
                1,
                null,
                null,
                "장소"
        );
    }

    private static CourseStop attractionStop(Long itemId, Long attractionId, int position) {
        return new CourseStop(
                itemId,
                CourseStopTarget.attraction(attractionId),
                position,
                1,
                null,
                null,
                "장소 " + attractionId
        );
    }

    private static Principal jwtPrincipal(String userId) {
        return () -> userId;
    }
}

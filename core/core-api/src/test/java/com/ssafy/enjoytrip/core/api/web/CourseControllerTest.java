package com.ssafy.enjoytrip.core.api.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import com.ssafy.enjoytrip.core.domain.AiCoursePreview;
import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseStop;
import com.ssafy.enjoytrip.core.domain.CourseStopTarget;
import com.ssafy.enjoytrip.core.domain.CourseOrderOptimizationContext;
import com.ssafy.enjoytrip.core.domain.query.DistanceSearchCondition;
import com.ssafy.enjoytrip.core.domain.service.AiCourseGenerationService;
import com.ssafy.enjoytrip.core.domain.service.CourseService;
import com.ssafy.enjoytrip.core.domain.vo.Coordinate;
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
    private AiCourseGenerationService aiCourseGenerationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        courseService = mock(CourseService.class);
        aiCourseGenerationService = mock(AiCourseGenerationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new CourseController(courseService, aiCourseGenerationService)
                )
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .alwaysDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .build();
    }

    @DisplayName("공개 코스 피드는 동네 이름과 한도로 조회하여 반환한다")
    @Test
    void returnsPublicCourseFeedCourses() throws Exception {
        when(courseService.findPublicFeed(anyString(), anyInt())).thenReturn(List.of(
                feedCourse("md-1", null, 42.5),
                feedCourse("course-1", 11L, 128.3)
        ));

        mockMvc.perform(get("/api/courses/feed")
                        .param("regionName", "망원동")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sections").doesNotExist())
                .andExpect(jsonPath("$.data.courses[0].id").value("md-1"))

                .andExpect(jsonPath("$.data.courses[0].distanceMeters").value(42.5))
                .andExpect(jsonPath("$.data.courses[0].startLocation.longitude").value(126.978))
                .andExpect(jsonPath("$.data.courses[0].startLocation.latitude").value(37.5665))
                .andExpect(jsonPath("$.data.courses[0].routeSummary.stopCount").value(2))
                .andExpect(jsonPath("$.data.courses[0].items[0].distanceToNext").value(140))
                .andExpect(jsonPath("$.data.courses[0].encodedPolyline").doesNotExist())
                .andExpect(jsonPath("$.data.courses[1].id").value("course-1"));

        verify(courseService).findPublicFeed("망원동", 20);
    }

    @DisplayName("공개 코스 피드는 동네 이름을 필수로 검증한다")
    @Test
    void rejectsPublicCourseFeedWithoutRegionName() throws Exception {
        mockMvc.perform(get("/api/courses/feed")
                        .param("limit", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(courseService, never()).findPublicFeed(anyString(), anyInt());
    }

    @DisplayName("공개 코스 피드는 limit 범위를 검증한다")
    @Test
    void rejectsPublicCourseFeedInvalidLimit() throws Exception {
        mockMvc.perform(get("/api/courses/feed")
                        .param("regionName", "망원동")
                        .param("limit", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(courseService, never()).findPublicFeed(anyString(), anyInt());
    }

    @DisplayName("공개 코스 상세는 경로 요약과 items를 반환한다")
    @Test
    void returnsPublicCourseDetail() throws Exception {
        when(courseService.view(eq("course-1"), any())).thenReturn(
                course("course-1", null, 0)
        );

        mockMvc.perform(get("/api/courses/course-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("course-1"))

                .andExpect(jsonPath("$.data.routeSummary.stopCount").value(2))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].distanceToNext").value(140))
                .andExpect(jsonPath("$.data.encodedPolyline").doesNotExist());
    }

    @DisplayName("인증 사용자는 본인 코스 목록에서 경로 요약을 확인한다")
    @Test
    void returnsMyCoursesWithRouteSummary() throws Exception {
        when(courseService.findMyCourses(11L)).thenReturn(List.of(
                course("course-1", 11L, 0)
        ));

        mockMvc.perform(get("/api/courses/me").principal(jwtPrincipal(11L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.courses[0].id").value("course-1"))
                .andExpect(jsonPath("$.data.courses[0].routeSummary.stopCount").value(2))
                .andExpect(jsonPath("$.data.courses[0].items").isArray())
                .andExpect(jsonPath("$.data.courses[0].encodedPolyline").doesNotExist());
    }

    @DisplayName("인증 사용자는 JSON 요청으로 본인 코스를 생성한다")
    @Test
    void authenticatedUserCreatesCourse() throws Exception {
        when(courseService.createCourse(any())).thenReturn(
                course("course-1", 11L, 0)
        );

        mockMvc.perform(post("/api/courses")
                        .principal(jwtPrincipal(11L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id":"course-1",
                                  "title":"망원 산책",
                                  "items":[
                                    {"itemType":"ATTRACTION","attractionId":1,"position":1},
                                    {"itemType":"ATTRACTION","attractionId":2,"position":2}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("course-1"))
                .andExpect(jsonPath("$.data.ownerMemberId").value(11))
                .andExpect(jsonPath("$.data.ownedUserId").value(11))
                .andExpect(jsonPath("$.data.routeSummary.stopCount").value(2))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].distanceToNext").value(140))
                .andExpect(jsonPath("$.data.encodedPolyline").doesNotExist());

        verify(courseService).createCourse(any());
    }


    @DisplayName("코스 생성 요청은 position 값이 아니라 items 배열 순서를 사용한다")
    @Test
    void createCourseUsesItemArrayOrderIgnoringPositions() throws Exception {
        when(courseService.createCourse(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/courses")
                        .principal(jwtPrincipal(11L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id":"course-array-order",
                                  "title":"망원 산책",
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
        assertThat(courseCaptor.getValue().stops()).extracting(stop -> stop.target().id())
                .containsExactly(2L, 1L);
        assertThat(courseCaptor.getValue().stops()).extracting(CourseStop::position)
                .containsExactly(1, 2);
    }

    @DisplayName("인증 사용자는 본인 코스를 수정하고 경로 요약 응답을 받는다")
    @Test
    void authenticatedUserUpdatesCourse() throws Exception {
        when(courseService.updateCourse(eq(11L), any())).thenReturn(
                course("course-1", 11L, 0)
        );

        mockMvc.perform(put("/api/courses/course-1")
                        .principal(jwtPrincipal(11L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"망원 산책 수정",
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
                .andExpect(jsonPath("$.data.encodedPolyline").doesNotExist());

        verify(courseService).updateCourse(eq(11L), any());
    }

    @DisplayName("코스 수정 요청은 position 값이 아니라 items 배열 순서를 사용한다")
    @Test
    void updateCourseUsesItemArrayOrderIgnoringPositions() throws Exception {
        when(courseService.updateCourse(eq(11L), any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        mockMvc.perform(put("/api/courses/course-1")
                        .principal(jwtPrincipal(11L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"망원 산책 수정",
                                  "items":[
                                    {"itemType":"ATTRACTION","attractionId":2,"position":99},
                                    {"itemType":"ATTRACTION","attractionId":1,"position":-10}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseService).updateCourse(eq(11L), courseCaptor.capture());
        assertThat(courseCaptor.getValue().stops()).extracting(stop -> stop.target().id())
                .containsExactly(2L, 1L);
        assertThat(courseCaptor.getValue().stops()).extracting(CourseStop::position)
                .containsExactly(1, 2);
    }

    @DisplayName("코스 순서 추천 미리보기는 저장된 아이템 id를 반환한다")
    @Test
    void recommendCourseOrderReturnsCourseResponseShape() throws Exception {
        when(courseService.recommendCourseOrder(eq(11L), eq("course-1"), any())).thenReturn(
                courseWithStoredStops("course-1", 11L, 101L, 102L)
        );

        mockMvc.perform(post("/api/courses/course-1/order-recommendation")
                        .principal(jwtPrincipal(11L))
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
                .andExpect(jsonPath("$.data.recommendationSource").doesNotExist())
                .andExpect(jsonPath("$.data.fallbackReason").doesNotExist())
                .andExpect(jsonPath("$.data.provider").doesNotExist());

        ArgumentCaptor<CourseOrderOptimizationContext> contextCaptor = ArgumentCaptor.forClass(
                CourseOrderOptimizationContext.class
        );
        verify(courseService).recommendCourseOrder(eq(11L), eq("course-1"), contextCaptor.capture());
        assertThat(contextCaptor.getValue().currentLatitude()).isEqualTo(37.5665);
        assertThat(contextCaptor.getValue().currentLongitude()).isEqualTo(126.9780);
    }

    private static Course course(String id, Long ownerMemberId, int saveCount) {
        return new Course(
                id,
                ownerMemberId,
                id,
                "망원동",
                null,
                new Coordinate(37.5665, 126.9780),
                null,
                saveCount,
                null,
                "",
                "",
                List.of(
                        attractionStop(null, 1L, 1, 140, 100),
                        attractionStop(null, 2L, 2, null, null)
                ),
                List.of()
        );
    }

    private static Course feedCourse(String id, Long ownerMemberId, Double distanceMeters) {
        return new Course(
                id,
                ownerMemberId,
                id,
                "망원동",
                null,
                new Coordinate(37.5665, 126.9780),
                distanceMeters,
                0,
                null,
                "",
                "",
                List.of(
                        attractionStop(null, 1L, 1, 140, 100),
                        attractionStop(null, 2L, 2, null, null)
                ),
                List.of()
        );
    }

    private static Course courseWithStoredStops(String id,
                                                Long ownerMemberId,
                                                Long firstItemId,
                                                Long secondItemId) {
        return new Course(
                id,
                ownerMemberId,
                id,
                "망원동",
                null,
                null,
                null,
                0,
                null,
                "",
                "",
                List.of(
                        attractionStop(firstItemId, 1L, 1, 140, 100),
                        attractionStop(secondItemId, 2L, 2, null, null)
                ),
                List.of()
        );
    }

    private static CourseStop attractionStop(Long itemId, Long attractionId, int position,
                                             Integer distanceToNext, Integer durationToNext) {
        return new CourseStop(
                itemId,
                CourseStopTarget.attraction(attractionId),
                position,
                "장소 " + attractionId,
                distanceToNext,
                durationToNext
        );
    }

    private static Principal jwtPrincipal(long memberId) {
        return () -> String.valueOf(memberId);
    }

    @DisplayName("POST /api/courses/ai-generate는 AI 코스 미리보기를 반환한다")
    @Test
    void generateAiCourseReturnsPreview() throws Exception {
        AiCoursePreview preview = new AiCoursePreview(
                "연인과 강남 감성 카페 투어",
                "감성적인 공간을 중심으로 구성했습니다.",
                List.of(
                        new AiCoursePreview.Stop(101L, "카페 어니언", "서울 강남구", "http://img.jpg"),
                        new AiCoursePreview.Stop(102L, "루프탑 카페", "서울 강남구", null)
                ),
                List.of("감성카페", "강남")
        );
        when(aiCourseGenerationService.generatePreview(
                anyLong(), any(), anyString(), anyList(), anyString(), anyInt()
        )).thenReturn(preview);

        mockMvc.perform(post("/api/courses/ai-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "regionName": "강남역",
                                  "companion": "WITH_PARTNER",
                                  "themes": ["CAFE", "PHOTO"],
                                  "pace": "RELAXED"
                                }
                                """)
                        .principal(jwtPrincipal(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("연인과 강남 감성 카페 투어"))
                .andExpect(jsonPath("$.data.reason").value("감성적인 공간을 중심으로 구성했습니다."))
                .andExpect(jsonPath("$.data.stops[0].attractionId").value(101))
                .andExpect(jsonPath("$.data.stops[1].attractionId").value(102));
    }

    @DisplayName("POST /api/courses/ai-generate는 companion 레이블로 서비스를 호출한다")
    @Test
    void generateAiCoursePassesCompanionLabel() throws Exception {
        when(aiCourseGenerationService.generatePreview(
                anyLong(), any(), anyString(), anyList(), anyString(), anyInt()
        )).thenReturn(new AiCoursePreview("타이틀", "이유", List.of(), List.of()));

        mockMvc.perform(post("/api/courses/ai-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "regionName": "망원동",
                                  "companion": "ALONE",
                                  "themes": ["WALK"],
                                  "pace": "MODERATE"
                                }
                                """)
                        .principal(jwtPrincipal(10L)))
                .andExpect(status().isOk());

        verify(aiCourseGenerationService).generatePreview(
                eq(10L), eq("망원동"), eq("혼자"), anyList(), eq("알맞게"), eq(4)
        );
    }

    @DisplayName("POST /api/courses/ai-generate는 regionName이 없으면 400을 반환한다")
    @Test
    void generateAiCourseReturnsBadRequestWhenRegionNameMissing() throws Exception {
        mockMvc.perform(post("/api/courses/ai-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companion": "ALONE",
                                  "themes": ["WALK"],
                                  "pace": "MODERATE"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}

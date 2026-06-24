package com.ssafy.enjoytrip.core.api.web;

import com.ssafy.enjoytrip.core.api.web.controller.*;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseRoute;
import com.ssafy.enjoytrip.core.domain.CourseRouteSegment;
import com.ssafy.enjoytrip.core.domain.CourseStop;
import com.ssafy.enjoytrip.core.domain.CourseStopTarget;
import com.ssafy.enjoytrip.core.domain.query.AttractionSearchCondition;
import com.ssafy.enjoytrip.core.domain.ChargerResult;
import com.ssafy.enjoytrip.core.domain.NewsResult;
import com.ssafy.enjoytrip.core.domain.NeighborhoodBriefing;
import com.ssafy.enjoytrip.core.domain.WeatherForecast;
import com.ssafy.enjoytrip.core.domain.WeatherSummary;
import com.ssafy.enjoytrip.core.domain.service.DbHealthService;
import com.ssafy.enjoytrip.core.domain.service.AttractionService;
import com.ssafy.enjoytrip.core.domain.service.AttractionStatsService;
import com.ssafy.enjoytrip.core.domain.service.BoardService;
import com.ssafy.enjoytrip.core.domain.service.EvChargerService;
import com.ssafy.enjoytrip.core.domain.service.HotplaceService;
import com.ssafy.enjoytrip.core.domain.service.CourseService;
import com.ssafy.enjoytrip.core.support.auth.JwtTokenService;
import com.ssafy.enjoytrip.core.domain.service.MemberProfileImageService;
import com.ssafy.enjoytrip.core.domain.service.MemberService;
import com.ssafy.enjoytrip.core.domain.service.NeighborhoodBriefingService;
import com.ssafy.enjoytrip.core.domain.service.NewsService;
import com.ssafy.enjoytrip.core.domain.service.NoticeService;
import com.ssafy.enjoytrip.core.support.auth.OAuthSignupTicketService;
import com.ssafy.enjoytrip.core.domain.service.PlanService;
import com.ssafy.enjoytrip.core.domain.service.WeatherService;
import com.ssafy.enjoytrip.core.domain.BoardPost;
import com.ssafy.enjoytrip.core.domain.Hotplace;
import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.domain.ProfileImageUploadUrl;
import com.ssafy.enjoytrip.core.domain.Notice;
import com.ssafy.enjoytrip.core.domain.TravelPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.time.Instant;
import java.util.List;

@ExtendWith(RestDocumentationExtension.class)
class ApiDocumentationTest {
    private MockMvc mockMvc;
    private AttractionService attractionService;
    private AttractionStatsService attractionStatsService;
    private EvChargerService chargerService;
    private NewsService newsService;
    private WeatherService weatherService;
    private NeighborhoodBriefingService neighborhoodBriefingService;
    private BoardService boardService;
    private HotplaceService hotplaceService;
    private PlanService planService;
    private NoticeService noticeService;
    private CourseService courseService;
    private MemberService memberService;
    private JwtTokenService tokenService;
    private OAuthSignupTicketService oauthSignupTicketService;
    private MemberProfileImageService memberProfileImageService;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        attractionService = mock(AttractionService.class);
        attractionStatsService = mock(AttractionStatsService.class);
        chargerService = mock(EvChargerService.class);
        newsService = mock(NewsService.class);
        weatherService = mock(WeatherService.class);
        neighborhoodBriefingService = mock(NeighborhoodBriefingService.class);
        boardService = mock(BoardService.class);
        hotplaceService = mock(HotplaceService.class);
        planService = mock(PlanService.class);
        noticeService = mock(NoticeService.class);
        courseService = mock(CourseService.class);
        memberService = mock(MemberService.class);
        tokenService = mock(JwtTokenService.class);
        oauthSignupTicketService = mock(OAuthSignupTicketService.class);
        memberProfileImageService = mock(MemberProfileImageService.class);

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new HealthController(mock(DbHealthService.class)),
                        new AttractionController(attractionService, attractionStatsService),
                        new ChargerController(chargerService),
                        new NewsController(newsService),
                        new WeatherController(weatherService),
                        new NeighborhoodBriefingController(neighborhoodBriefingService),
                        new BoardController(boardService),
                        new HotplaceController(hotplaceService),
                        new PlanController(planService),
                        new NoticeController(noticeService),
                        new CourseController(courseService),
                        new MemberController(memberService, tokenService, oauthSignupTicketService),
                        new MemberProfileImageController(memberProfileImageService)
                )
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @DisplayName("헬스 API 문서를 검증한다")
    @Test
    void health() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ok"))
                .andDo(document("health",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("관광지 API 문서를 검증한다")
    @Test
    void attractions() throws Exception {
        when(attractionService.searchAttractions(new AttractionSearchCondition(
                1,
                null,
                null,
                "궁",
                null,
                null,
                null
        )))
                .thenReturn(List.of(
                new Attraction(
                        1L, "경복궁", "서울 종로구", "", "", "", "", "", 0, 1, 1, 37.5796, 126.9770, "6", "", "",
                        0, 0.0, 0, List.of(), false, null)
        ));

        mockMvc.perform(get("/api/attractions").param("sidoCode", "1").param("keyword", "궁"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attractions").isArray())
                .andDo(document("attractions",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("충전소 API 문서를 검증한다")
    @Test
    void chargers() throws Exception {
        when(chargerService.findChargers(null, "서울", 1, 150)).thenReturn(List.of(
                new ChargerResult(
                        "ST001", "서울충전소", "01", "06", "서울", "", 37.5, 127.0, "24시간", "환경부", "1661-9408", "2"
                )
        ));

        mockMvc.perform(get("/api/chargers").param("keyword", "서울"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.chargers").isArray())
                .andDo(document("chargers",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("뉴스 API 문서를 검증한다")
    @Test
    void news() throws Exception {
        when(newsService.findNews()).thenReturn(List.of(
                new NewsResult("news_1", "관광 뉴스", "https://example.com", "요약", "관광 뉴스", "2026-05-14")
        ));

        mockMvc.perform(get("/api/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.news").isArray())
                .andDo(document("news",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("날씨 브리핑 API 문서를 검증한다")
    @Test
    void weatherBriefings() throws Exception {
        when(weatherService.findWeatherBriefings()).thenReturn(List.of(
                new WeatherSummary("서울", "맑음", 22, 10, "05:23", "19:33", 15, 25)
        ));

        mockMvc.perform(get("/api/weather/briefings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weather").isArray())
                .andDo(document("weather-briefings",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("동네 AI 브리핑 API 문서를 검증한다")
    @Test
    void neighborhoodBriefing() throws Exception {
        WeatherSummary weather = new WeatherSummary("서울", "맑음", 22, 10, "05:23", "19:33", 15, 25);
        List<WeatherForecast> forecasts = List.of(
                new WeatherForecast("12:00", 22, "맑음", 10),
                new WeatherForecast("13:00", 23, "맑음", 10),
                new WeatherForecast("14:00", 24, "맑음", 10),
                new WeatherForecast("15:00", 25, "구름 많음", 20),
                new WeatherForecast("16:00", 24, "구름 많음", 20),
                new WeatherForecast("17:00", 23, "맑음", 10)
        );
        when(neighborhoodBriefingService.brief(eq("서울"), any(), any(), anyString()))
                .thenReturn(new NeighborhoodBriefing(
                        "서울",
                        "오늘 서울은 맑고 더운 편이라 한강 저녁 산책 코스 어떠세요?",
                        weather,
                        forecasts
                ));

        mockMvc.perform(get("/api/neighborhood/briefing").param("regionName", "서울"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.season").doesNotExist())
                .andExpect(jsonPath("$.data.briefing").isNotEmpty())
                .andExpect(jsonPath("$.data.forecasts.length()").value(6))
                .andExpect(jsonPath("$.data.courseId").doesNotExist())
                .andDo(document("neighborhood-briefing",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("게시글 API 문서를 검증한다")
    @Test
    void boards() throws Exception {
        when(boardService.findAllPosts()).thenReturn(List.of(
                new BoardPost("b1", "제목", "내용", "ssafy", "2026-05-14 11:00:00", "")
        ));

        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.boards").isArray())
                .andDo(document("boards",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("핫플레이스 API 문서를 검증한다")
    @Test
    void hotplaces() throws Exception {
        when(hotplaceService.findAllHotplaces()).thenReturn(List.of(
                new Hotplace(
                        "h1", 11L, "남산", "view", "2026-05-14", 37.55, 126.99, "야경", "",
                        "2026-05-14 11:00:00"
                )
        ));

        mockMvc.perform(get("/api/hotplaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hotplaces").isArray())
                .andDo(document("hotplaces",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("여행 계획 API 문서를 검증한다")
    @Test
    void plans() throws Exception {
        when(planService.findAllPlans()).thenReturn(List.of(
                new TravelPlan(
                        "p1", 11L, "서울 여행", "2026-05-14", "2026-05-15", 100000, "메모", "[]",
                        "2026-05-14 11:00:00"
                )
        ));

        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.plans").isArray())
                .andDo(document("plans",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("코스 API 문서를 검증한다")
    @Test
    void courses() throws Exception {
        Course course = course("course-1", 11L, 101L, 102L);
        when(courseService.createCourse(any())).thenReturn(course);
        when(courseService.updateCourse(eq(11L), any())).thenReturn(course);
        when(courseService.recommendCourseOrder(eq(11L), eq("course-1"), any())).thenReturn(course);

        mockMvc.perform(post("/api/courses")
                        .principal(() -> "11")
                        .contentType("application/json")
                        .content("""
                                {
                                  "id": "course-1",
                                  "title": "서울 산책",
                                  "visibility": "PRIVATE",
                                  "status": "READY",
                                  "items": [
                                    {"itemType": "ATTRACTION", "attractionId": 1},
                                    {"itemType": "ATTRACTION", "attractionId": 2}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].id").value(101L))
                .andExpect(jsonPath("$.data.items[0].position").value(1))
                .andDo(document("courses-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));

        mockMvc.perform(put("/api/courses/course-1")
                        .principal(() -> "11")
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "서울 산책 수정",
                                  "visibility": "PRIVATE",
                                  "items": [
                                    {"itemType": "ATTRACTION", "attractionId": 2},
                                    {"itemType": "ATTRACTION", "attractionId": 1}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.routeSummary.stopCount").value(2))
                .andDo(document("courses-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));

        mockMvc.perform(post("/api/courses/course-1/order-recommendation")
                        .principal(() -> "11")
                        .contentType("application/json")
                        .content("""
                                {
                                  "currentLatitude": 37.5665,
                                  "currentLongitude": 126.9780
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].id").value(101L))
                .andExpect(jsonPath("$.data.items[0].position").value(1))
                .andExpect(jsonPath("$.data.routeSummary.stopCount").value(2))
                .andExpect(jsonPath("$.data.recommendationSource").doesNotExist())
                .andExpect(jsonPath("$.data.fallbackReason").doesNotExist())
                .andExpect(jsonPath("$.data.provider").doesNotExist())
                .andDo(document("courses-order-recommendation",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("공지 API 문서를 검증한다")
    @Test
    void notices() throws Exception {
        when(noticeService.findAllNotices()).thenReturn(List.of(
                new Notice(1L, "공지", "내용", "admin", "2026-05-14 11:00:00", "")
        ));

        mockMvc.perform(get("/api/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notices").isArray())
                .andDo(document("notices",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("회원 API 문서를 검증한다")
    @Test
    void members() throws Exception {
        when(memberService.findAllUsers()).thenReturn(List.of(
                new Member(null, "SSAFY", "ssafy", "ssafy@example.com", "secret", null)
        ));

        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.users").isArray())
                .andDo(document("members",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }


    @DisplayName("회원 프로필 이미지 API 문서를 검증한다")
    @Test
    void memberProfileImages() throws Exception {
        String objectKey = "profiles/11/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg";
        when(memberProfileImageService.createPresignedUpload(11L, "image/jpeg", "jpg"))
                .thenReturn(new ProfileImageUploadUrl(
                        objectKey,
                        "http://localhost:9000/dongnepin-notes/" + objectKey + "?signature=abc",
                        Instant.parse("2026-06-22T05:10:00Z"),
                        "http://localhost:9000/dongnepin-notes/" + objectKey
                ));

        mockMvc.perform(post("/api/members/me/profile-image/presigned-upload")
                        .principal(() -> "11")
                        .contentType("application/json")
                        .content("""
                                {
                                  "contentType": "image/jpeg",
                                  "fileExtension": "jpg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.objectKey").value(objectKey))
                .andDo(document("member-profile-image-presigned-upload",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));

        mockMvc.perform(put("/api/members/me/profile-image")
                        .principal(() -> "11")
                        .contentType("application/json")
                        .content("""
                                {
                                  "objectKey": "%s",
                                  "contentType": "image/jpeg"
                                }
                                """.formatted(objectKey)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("member-profile-image-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    private static Course course(String id, Long ownerMemberId, Long firstItemId, Long secondItemId) {
        return new Course(
                id,
                ownerMemberId,
                "서울 산책",
                "서울",
                "PRIVATE",
                "READY",
                null,
                null,
                null,
                null,
                false,
                0,
                "2026-06-23T00:00:00",
                "2026-06-23T00:00:00",
                CourseRoute.planned(
                        List.of(
                                attractionStop(firstItemId, 1L, 1),
                                attractionStop(secondItemId, 2L, 2)
                        ),
                        List.of(new CourseRouteSegment(1, 1, 2, "WALK", 100, 140))
                )
        );
    }

    private static CourseStop attractionStop(Long itemId, Long attractionId, int position) {
        return new CourseStop(
                itemId,
                CourseStopTarget.attraction(attractionId),
                position,
                1,
                null,
                60,
                "장소 " + attractionId
        );
    }
}

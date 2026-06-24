package com.ssafy.enjoytrip.core.api.web;

import com.ssafy.enjoytrip.core.api.web.controller.*;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.vo.Address;
import com.ssafy.enjoytrip.core.domain.vo.Coordinate;
import com.ssafy.enjoytrip.core.domain.vo.RatingStats;
import com.ssafy.enjoytrip.core.domain.vo.TemperatureRange;
import com.ssafy.enjoytrip.core.domain.vo.DateRange;
import com.ssafy.enjoytrip.core.domain.CourseStop;
import com.ssafy.enjoytrip.core.domain.CourseStopTarget;
import com.ssafy.enjoytrip.core.domain.query.AttractionSearchCondition;

import com.ssafy.enjoytrip.core.domain.NeighborhoodBriefing;
import com.ssafy.enjoytrip.core.domain.WeatherForecast;
import com.ssafy.enjoytrip.core.domain.WeatherSummary;
import com.ssafy.enjoytrip.core.domain.WeatherWithForecast;
import com.ssafy.enjoytrip.core.domain.service.DbHealthService;
import com.ssafy.enjoytrip.core.domain.service.AttractionService;
import com.ssafy.enjoytrip.core.domain.service.AttractionStatsService;
import com.ssafy.enjoytrip.core.domain.service.CourseService;
import com.ssafy.enjoytrip.core.support.auth.JwtTokenService;
import com.ssafy.enjoytrip.core.domain.service.MemberProfileImageService;
import com.ssafy.enjoytrip.core.domain.service.MemberService;
import com.ssafy.enjoytrip.core.domain.service.NeighborhoodBriefingService;
import com.ssafy.enjoytrip.core.domain.service.WeatherService;
import com.ssafy.enjoytrip.core.support.auth.OAuthSignupTicketService;
import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.domain.ProfileImageUploadUrl;
import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import com.ssafy.enjoytrip.core.domain.service.MapExploreService;
import com.ssafy.enjoytrip.core.domain.service.MapSearchService;
import com.ssafy.enjoytrip.core.domain.MapExploreResult;
import com.ssafy.enjoytrip.core.domain.MapCenter;
import com.ssafy.enjoytrip.core.domain.PlaceMapPin;
import com.ssafy.enjoytrip.core.domain.NoteMapPin;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NoteVisibility;
import com.ssafy.enjoytrip.core.domain.NoteViewerRelationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

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
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;

import java.time.Instant;
import java.util.List;

@ExtendWith(RestDocumentationExtension.class)
class ApiDocumentationTest {
    private MockMvc mockMvc;
    private AttractionService attractionService;
    private AttractionStatsService attractionStatsService;
    private WeatherService weatherService;
    private NeighborhoodBriefingService neighborhoodBriefingService;
    private CourseService courseService;
    private MemberService memberService;
    private JwtTokenService tokenService;
    private OAuthSignupTicketService oauthSignupTicketService;
    private MemberProfileImageService memberProfileImageService;
    private MapExploreService mapExploreService;
    private MapSearchService mapSearchService;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        attractionService = mock(AttractionService.class);
        attractionStatsService = mock(AttractionStatsService.class);
        weatherService = mock(WeatherService.class);
        neighborhoodBriefingService = mock(NeighborhoodBriefingService.class);
        courseService = mock(CourseService.class);
        memberService = mock(MemberService.class);
        tokenService = mock(JwtTokenService.class);
        oauthSignupTicketService = mock(OAuthSignupTicketService.class);
        memberProfileImageService = mock(MemberProfileImageService.class);
        mapExploreService = mock(MapExploreService.class);
        mapSearchService = mock(MapSearchService.class);

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new HealthController(mock(DbHealthService.class)),
                        new AttractionController(attractionService, attractionStatsService),
                        new NeighborhoodBriefingController(neighborhoodBriefingService, weatherService),
                        new CourseController(courseService),
                        new MemberController(memberService, tokenService, oauthSignupTicketService),
                        new MemberProfileImageController(memberProfileImageService),
                        new MapController(mapExploreService, mapSearchService)
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
                        1L, "경복궁", new Address("서울 종로구", "", ""), "", "", "", 0, 1, 1,
                        new Coordinate(37.5796, 126.9770), "6", "", "",
                        0, new RatingStats(0.0, 0), false, null)
        ));

        mockMvc.perform(get("/api/attractions").param("sidoCode", "1").param("keyword", "궁"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attractions").isArray())
                .andDo(document("attractions",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("관광지 상세 API 문서를 검증한다")
    @Test
    void attractionDetail() throws Exception {
        when(attractionService.findAttractionDetail(1L, null))
                .thenReturn(new Attraction(
                        1L,
                        "경복궁",
                        new Address("서울 종로구", "", "03045"),
                        "02-3700-3900",
                        "https://example.com/gyeongbokgung.jpg",
                        "",
                        42,
                        1,
                        1,
                        new Coordinate(37.5796, 126.9770),
                        "6",
                        "12",
                        "조선 시대 궁궐입니다.",
                        12,
                        new RatingStats(4.5, 8),
                        false,
                        null
                ));

        mockMvc.perform(get("/api/attractions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.imageUrl").value("https://example.com/gyeongbokgung.jpg"))
                .andExpect(jsonPath("$.data.overview").value("조선 시대 궁궐입니다."))
                .andDo(document("attraction-detail",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }





    @DisplayName("동네 AI 브리핑 API 문서를 검증한다")
    @Test
    void neighborhoodBriefing() throws Exception {
        WeatherSummary weather = new WeatherSummary("서울", "맑음", 22, 10, "05:23", "19:33", new TemperatureRange(15, 25));
        List<WeatherForecast> forecasts = List.of(
                new WeatherForecast("12:00", 22, "맑음", 10),
                new WeatherForecast("13:00", 23, "맑음", 10),
                new WeatherForecast("14:00", 24, "맑음", 10),
                new WeatherForecast("15:00", 25, "구름 많음", 20),
                new WeatherForecast("16:00", 24, "구름 많음", 20),
                new WeatherForecast("17:00", 23, "맑음", 10)
        );
        WeatherWithForecast weatherWithForecast = new WeatherWithForecast(weather, forecasts);
        when(weatherService.findWeatherWithForecast(any(), any(), eq("서울"), anyString()))
                .thenReturn(weatherWithForecast);
        when(neighborhoodBriefingService.brief(eq("서울"), any(WeatherWithForecast.class), anyString()))
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
                "2026-06-23",
                false,
                null,
                null,
                0,
                "2026-06-23T00:00:00",
                "2026-06-23T00:00:00",
                List.of(
                        attractionStop(firstItemId, 1L, 1),
                        attractionStop(secondItemId, 2L, 2)
                ),
                List.of()
        );
    }

    private static CourseStop attractionStop(Long itemId, Long attractionId, int position) {
        return new CourseStop(
                itemId,
                CourseStopTarget.attraction(attractionId),
                position,
                "장소 " + attractionId,
                position < 2 ? 140 : null,
                position < 2 ? 100 : null
        );
    }

    @DisplayName("지도 탐색 API 문서를 검증한다")
    @Test
    void mapExplore() throws Exception {
        when(mapExploreService.explore(eq(11L), eq(126.9780), eq(37.5665), eq(500.0), any(), any()))
                .thenReturn(new MapExploreResult(
                        new MapCenter(126.9780, 37.5665, "서울 중구"),
                        500.0,
                        MapExploreFilter.ALL,
                        List.of(new PlaceMapPin(125405L, "경복궁", "서울 중구", 37.579617, 126.977041, "https://cdn.example.com/place.png", "12", 1450.2, true, 12, 4.5, 8, 0)),
                        List.of(new NoteMapPin(1L, "서울 산책 메모", NoteCategory.TIP, NoteVisibility.PUBLIC, 37.5665, 126.9780, "서울 중구", 42.0, null, "동네핀러", null, NoteViewerRelationship.NONE, LocalDateTime.of(2026, 6, 22, 10, 0, 0), 1))
                ));

        mockMvc.perform(get("/api/map/explore")
                        .principal(() -> "11")
                        .param("mapX", "126.9780")
                        .param("mapY", "37.5665")
                        .param("radius", "500.0")
                        .param("filter", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("map-explore",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("지도 키워드 검색 API 문서를 검증한다")
    @Test
    void mapSearch() throws Exception {
        when(mapSearchService.search(anyString(), anyDouble(), anyDouble(), any(), any(), any(), anyInt(), eq(11L)))
                .thenReturn(List.of(
                        new PlaceMapPin(125405L, "경복궁", "서울 중구", 37.579617, 126.977041, "https://cdn.example.com/place.png", "12", 1450.2, true, 12, 4.5, 8, 0),
                        new NoteMapPin(1L, "서울 산책 메모", NoteCategory.TIP, NoteVisibility.PUBLIC, 37.5665, 126.9780, "서울 중구", 42.0, null, "동네핀러", null, NoteViewerRelationship.NONE, LocalDateTime.of(2026, 6, 22, 10, 0, 0), 1)
                ));

        mockMvc.perform(get("/api/map/search")
                        .principal(() -> "11")
                        .param("keyword", "경복궁")
                        .param("mapX", "126.9780")
                        .param("mapY", "37.5665")
                        .param("radius", "500.0")
                        .param("target", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].type").value("PLACE"))
                .andExpect(jsonPath("$.data[1].type").value("NOTE"))
                .andDo(document("map-search",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("지도 키워드 검색 시 키워드가 없으면 400 에러를 반환한다")
    @Test
    void mapSearchMissingKeywordThrows400() throws Exception {
        mockMvc.perform(get("/api/map/search")
                        .principal(() -> "11")
                        .param("mapX", "126.9780")
                        .param("mapY", "37.5665"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}

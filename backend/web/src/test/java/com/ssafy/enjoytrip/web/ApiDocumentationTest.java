package com.ssafy.enjoytrip.web;
import com.ssafy.enjoytrip.web.api.*;
import com.ssafy.enjoytrip.web.controller.*;
import com.ssafy.enjoytrip.web.dto.request.*;
import com.ssafy.enjoytrip.web.dto.response.*;

import com.ssafy.enjoytrip.service.RouteOptimizationService;
import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.domain.AttractionSearchCondition;
import com.ssafy.enjoytrip.domain.ChargerItem;
import com.ssafy.enjoytrip.domain.NewsItem;
import com.ssafy.enjoytrip.domain.WeatherSummary;
import com.ssafy.enjoytrip.repository.DbHealthRepository;
import com.ssafy.enjoytrip.service.AttractionService;
import com.ssafy.enjoytrip.service.BoardService;
import com.ssafy.enjoytrip.service.EvChargerService;
import com.ssafy.enjoytrip.service.HotplaceService;
import com.ssafy.enjoytrip.service.JwtTokenService;
import com.ssafy.enjoytrip.service.MemberService;
import com.ssafy.enjoytrip.service.NewsService;
import com.ssafy.enjoytrip.service.NoticeService;
import com.ssafy.enjoytrip.service.OAuthSignupTicketService;
import com.ssafy.enjoytrip.service.PlanService;
import com.ssafy.enjoytrip.web.mapper.PlanResponseAssembler;
import com.ssafy.enjoytrip.service.WeatherService;
import com.ssafy.enjoytrip.domain.BoardPost;
import com.ssafy.enjoytrip.domain.Hotplace;
import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.domain.Notice;
import com.ssafy.enjoytrip.domain.TravelPlan;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

@ExtendWith(RestDocumentationExtension.class)
class ApiDocumentationTest {
    private MockMvc mockMvc;
    private AttractionService attractionService;
    private EvChargerService chargerService;
    private NewsService newsService;
    private WeatherService weatherService;
    private BoardService boardService;
    private HotplaceService hotplaceService;
    private PlanService planService;
    private NoticeService noticeService;
    private MemberService memberService;
    private JwtTokenService tokenService;
    private OAuthSignupTicketService oauthSignupTicketService;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        attractionService = mock(AttractionService.class);
        chargerService = mock(EvChargerService.class);
        newsService = mock(NewsService.class);
        weatherService = mock(WeatherService.class);
        boardService = mock(BoardService.class);
        hotplaceService = mock(HotplaceService.class);
        planService = mock(PlanService.class);
        noticeService = mock(NoticeService.class);
        memberService = mock(MemberService.class);
        tokenService = mock(JwtTokenService.class);
        oauthSignupTicketService = mock(OAuthSignupTicketService.class);

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new HealthController(mock(DbHealthRepository.class)),
                        new RouteController(new RouteOptimizationService()),
                        new AttractionController(attractionService),
                        new ChargerController(chargerService),
                        new NewsController(newsService),
                        new WeatherController(weatherService),
                        new BoardController(boardService),
                        new HotplaceController(hotplaceService),
                        new PlanController(
                                planService,
                                new PlanResponseAssembler(planService)
                        ),
                        new NoticeController(noticeService),
                        new MemberController(memberService, tokenService, oauthSignupTicketService)
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

    @DisplayName("경로 최적화 API 문서를 검증한다")
    @Test
    void routeOptimize() throws Exception {
        mockMvc.perform(get("/api/routes/optimizations")
                        .param("points", "37.5665,126.9780|35.1796,129.0756|33.4996,126.5312"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.order").isArray())
                .andDo(document("route-optimize",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("일자별 경로 분할 API 문서를 검증한다")
    @Test
    void routeSplitByDay() throws Exception {
        mockMvc.perform(get("/api/routes/day-splits")
                        .param("points", "37.5665,126.9780|35.1796,129.0756|33.4996,126.5312")
                        .param("days", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.days").isArray())
                .andDo(document("route-split-by-day",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @DisplayName("관광지 API 문서를 검증한다")
    @Test
    void attractions() throws Exception {
        when(attractionService.searchAttractions(new AttractionSearchCondition("1", "", "", "궁", "", "", "")))
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
        when(chargerService.findChargers("", "서울", 1, 150)).thenReturn(List.of(
                new ChargerItem(
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
                new NewsItem("news_1", "관광 뉴스", "https://example.com", "요약", "관광 뉴스", "2026-05-14")
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
                new WeatherSummary("서울", "맑음", 22, 10, "05:23", "19:33")
        ));

        mockMvc.perform(get("/api/weather/briefings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weather").isArray())
                .andDo(document("weather-briefings",
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
                        "h1", "ssafy", "남산", "view", "2026-05-14", 37.55, 126.99, "야경", "",
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
                        "p1", "ssafy", "서울 여행", "2026-05-14", "2026-05-15", 100000, "메모", "[]",
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
                new Member("ssafy", "SSAFY", "ssafy@example.com", "secret", "2026-05-14 11:00:00")
        ));

        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.users").isArray())
                .andDo(document("members",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }
}

package com.ssafy.enjoytrip.web;
import com.ssafy.enjoytrip.web.api.*;
import com.ssafy.enjoytrip.web.controller.*;
import com.ssafy.enjoytrip.web.dto.request.*;
import com.ssafy.enjoytrip.web.dto.response.*;

import com.ssafy.enjoytrip.domain.BoardPost;
import com.ssafy.enjoytrip.domain.Hotplace;
import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.domain.Notice;
import com.ssafy.enjoytrip.domain.PlanItem;
import com.ssafy.enjoytrip.domain.TravelPlan;
import com.ssafy.enjoytrip.domain.AttractionSearchCondition;
import com.ssafy.enjoytrip.domain.AttractionStats;
import com.ssafy.enjoytrip.domain.AttractionTag;
import com.ssafy.enjoytrip.domain.WeatherSummary;
import com.ssafy.enjoytrip.exception.ExternalServiceException;
import com.ssafy.enjoytrip.repository.DbHealthRepository;
import com.ssafy.enjoytrip.service.AttractionService;
import com.ssafy.enjoytrip.service.BoardService;
import com.ssafy.enjoytrip.service.EvChargerService;
import com.ssafy.enjoytrip.service.HotplaceService;
import com.ssafy.enjoytrip.service.JwtTokenService;
import com.ssafy.enjoytrip.service.MemberService;
import com.ssafy.enjoytrip.service.NoticeService;
import com.ssafy.enjoytrip.service.OAuthSignupTicketService;
import com.ssafy.enjoytrip.service.PlanService;
import com.ssafy.enjoytrip.service.RouteOptimizationService;
import com.ssafy.enjoytrip.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("web")
class ControllerBehaviorTest {
    private BoardService boardService;
    private HotplaceService hotplaceService;
    private PlanService planService;
    private NoticeService noticeService;
    private MemberService memberService;
    private JwtTokenService tokenService;
    private OAuthSignupTicketService oauthSignupTicketService;
    private AttractionService attractionService;
    private EvChargerService chargerService;
    private WeatherService weatherService;
    private DbHealthRepository dbHealthRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        boardService = mock(BoardService.class);
        hotplaceService = mock(HotplaceService.class);
        planService = mock(PlanService.class);
        noticeService = mock(NoticeService.class);
        memberService = mock(MemberService.class);
        tokenService = mock(JwtTokenService.class);
        oauthSignupTicketService = mock(OAuthSignupTicketService.class);
        attractionService = mock(AttractionService.class);
        chargerService = mock(EvChargerService.class);
        weatherService = mock(WeatherService.class);
        dbHealthRepository = mock(DbHealthRepository.class);

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new BoardController(boardService),
                        new HotplaceController(hotplaceService),
                        new PlanController(planService),
                        new NoticeController(noticeService),
                        new MemberController(memberService, tokenService, oauthSignupTicketService),
                        new AttractionController(attractionService),
                        new AttractionTagController(attractionService),
                        new ChargerController(chargerService),
                        new WeatherController(weatherService),
                        new RouteController(new RouteOptimizationService()),
                        new HealthController(dbHealthRepository),
                        new FailingController()
                )
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    class WeatherEndpoints {
        @Test
        void returnsWeatherBriefingsAndDelegatesToService() throws Exception {
            when(weatherService.findWeatherBriefings()).thenReturn(List.of(
                    new WeatherSummary("서울", "맑음", 22, 10, "05:23", "19:33"),
                    new WeatherSummary("부산", "구름 많음", 21, 20, "05:17", "19:22")
            ));

            mockMvc.perform(get("/api/weather/briefings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.weather[0].region").value("서울"))
                    .andExpect(jsonPath("$.data.weather[0].condition").value("맑음"))
                    .andExpect(jsonPath("$.data.weather[0].temperature").value(22))
                    .andExpect(jsonPath("$.data.weather[0].rainChance").value(10))
                    .andExpect(jsonPath("$.data.weather[0].sunrise").value("05:23"))
                    .andExpect(jsonPath("$.data.weather[0].sunset").value("19:33"));

            verify(weatherService).findWeatherBriefings();
        }
    }

    @Nested
    class BoardEndpoints {
        @Test
        void createTrimsAndPassesBoardPostToService() throws Exception {
            mockMvc.perform(post("/api/boards/posts")
                            .param("id", " b1 ")
                            .param("title", " title ")
                            .param("content", " content ")
                            .param("author", " ssafy "))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            ArgumentCaptor<BoardPost> captor = ArgumentCaptor.forClass(BoardPost.class);
            verify(boardService).insertPost(captor.capture());
            assertThat(captor.getValue()).isEqualTo(new BoardPost("b1", "title", "content", "ssafy", "", ""));
        }

        @Test
        void reportsValidationActionNotFoundAndServiceExceptionCases() throws Exception {
            mockMvc.perform(post("/api/boards/posts").param("id", "b1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

            mockMvc.perform(post("/api/boards").param("action", "unknown"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Invalid action"));

            when(boardService.updatePost(any())).thenReturn(false);
            mockMvc.perform(put("/api/boards/b1").param("title", "title").param("content", "content"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message").value("Post not found"));

            doThrow(new IllegalStateException("write failed")).when(boardService).insertPost(any());
            mockMvc.perform(post("/api/boards/posts")
                            .param("id", "b2")
                            .param("title", "title")
                            .param("content", "content")
                            .param("author", "ssafy"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"));
        }
    }

    @Nested
    class HotplaceEndpoints {
        @Test
        void findsHotplacesByUserAndCreatesWithCoordinates() throws Exception {
            when(hotplaceService.findHotplacesByUser("ssafy")).thenReturn(List.of(
                    new Hotplace("h1", "ssafy", "남산", "view", "2026-05-14", 37.55, 126.99, "night", "", "created")
            ));

            mockMvc.perform(get("/api/hotplaces").param("userId", " ssafy "))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.hotplaces[0].id").value("h1"));

            mockMvc.perform(post("/api/hotplaces/items")
                            .param("id", "h2")
                            .param("userId", "ssafy")
                            .param("title", "광안리")
                            .param("type", "beach")
                            .param("visitDate", "2026-05-15")
                            .param("lat", "35.153")
                            .param("lng", "129.118")
                            .param("description", "sea"))
                    .andExpect(status().isOk());

            ArgumentCaptor<Hotplace> captor = ArgumentCaptor.forClass(Hotplace.class);
            verify(hotplaceService).insertHotplace(captor.capture());
            assertThat(captor.getValue().lat()).isEqualTo(35.153);
            assertThat(captor.getValue().lng()).isEqualTo(129.118);
        }

        @Test
        void rejectsInvalidCoordinatesAndMissingDeleteTarget() throws Exception {
            mockMvc.perform(post("/api/hotplaces/items")
                            .param("id", "h1")
                            .param("userId", "ssafy")
                            .param("title", "남산")
                            .param("type", "view")
                            .param("visitDate", "2026-05-14")
                            .param("lat", "north")
                            .param("lng", "126.99"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Invalid latitude or longitude"));

            when(hotplaceService.deleteHotplace("h-missing")).thenReturn(false);
            mockMvc.perform(delete("/api/hotplaces/h-missing"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message").value("Hotplace not found"));
        }
    }

    @Nested
    class PlanAndNoticeEndpoints {
        @Test
        void planFindParsesRouteItemsAndFallsBackToEmptyArrayForInvalidJson() throws Exception {
            when(planService.findPlansByUser("ssafy")).thenReturn(List.of(
                    new TravelPlan("p1", "ssafy", "서울", "2026-05-14", "2026-05-15", 1000, null, "[{\"title\":\"A\"}]", "created"),
                    new TravelPlan("p2", "ssafy", "부산", "2026-05-16", "2026-05-17", 2000, "note", "not json", "created")
            ));

            mockMvc.perform(get("/api/plans").param("userId", " ssafy "))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.plans[0].routeItems[0].title").value("A"))
                    .andExpect(jsonPath("$.data.plans[0].note").value(""))
                    .andExpect(jsonPath("$.data.plans[1].routeItems", empty()));
        }

        @Test
        void planCreateDefaultsInvalidBudgetAndDeleteMissingReturnsNotFound() throws Exception {
            mockMvc.perform(post("/api/plans/items")
                            .principal(jwtPrincipal("ssafy"))
                            .param("id", "p1")
                            .param("userId", "ssafy")
                            .param("title", "서울")
                            .param("startDate", "2026-05-14")
                            .param("endDate", "2026-05-15")
                            .param("budget", "many")
                            .param("routeItems", ""))
                    .andExpect(status().isOk());

            ArgumentCaptor<TravelPlan> captor = ArgumentCaptor.forClass(TravelPlan.class);
            verify(planService).insertPlan(captor.capture(), any());
            assertThat(captor.getValue().budget()).isZero();
            assertThat(captor.getValue().routeItemsJson()).isEqualTo("[]");

            when(planService.findPlan("missing")).thenReturn(java.util.Optional.empty());
            mockMvc.perform(delete("/api/plans/missing").principal(jwtPrincipal("ssafy")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message").value("Plan not found"));
        }

        @Test
        void planCreateStoresNormalizedRouteItemsFromAuthenticatedUser() throws Exception {
            mockMvc.perform(post("/api/plans/items")
                            .principal(jwtPrincipal("ssafy"))
                            .param("id", "p-route")
                            .param("userId", "ignored")
                            .param("title", "서울")
                            .param("startDate", "2026-05-14")
                            .param("endDate", "2026-05-15")
                            .param("routeItems", "[{\"id\":10,\"day\":2,\"memo\":\"lunch\",\"stayMinutes\":120},{\"attractionId\":11}]"))
                    .andExpect(status().isOk());

            ArgumentCaptor<TravelPlan> planCaptor = ArgumentCaptor.forClass(TravelPlan.class);
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<PlanItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
            verify(planService).insertPlan(planCaptor.capture(), itemsCaptor.capture());
            assertThat(planCaptor.getValue().userId()).isEqualTo("ssafy");

            assertThat(itemsCaptor.getValue()).extracting(PlanItem::attractionId).containsExactly(10L, 11L);
            assertThat(itemsCaptor.getValue().getFirst().day()).isEqualTo(2);
            assertThat(itemsCaptor.getValue().getFirst().memo()).isEqualTo("lunch");
        }

        @Test
        void noticeCreateUpdateAndDeleteValidationCases() throws Exception {
            mockMvc.perform(post("/api/notices/items")
                            .param("title", "공지")
                            .param("content", "내용")
                            .param("author", "admin"))
                    .andExpect(status().isOk());
            verify(noticeService).insertNotice(any());

            mockMvc.perform(put("/api/notices/0").param("title", "공지").param("content", "내용"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Invalid request"));

            when(noticeService.updateNotice(any())).thenReturn(false);
            mockMvc.perform(put("/api/notices/1").param("title", "공지").param("content", "내용"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message").value("Notice not found"));

            mockMvc.perform(post("/api/notices").param("action", "delete").param("id", "not-a-number"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Invalid id"));
        }
    }

    @Nested
    class MemberAuthEndpoints {
        @Test
        void loginFailureLogoutValidationAndPasswordLookupGone() throws Exception {
            when(memberService.login("ssafy", "wrong")).thenReturn(null);

            mockMvc.perform(post("/api/members/login").param("userId", "ssafy").param("password", "wrong"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.message").value("Invalid credentials"));

            mockMvc.perform(post("/api/members/logout"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Missing userId"));

            mockMvc.perform(post("/api/members/find-password").param("userId", "ssafy").param("email", "ssafy@example.com"))
                    .andExpect(status().isGone())
                    .andExpect(jsonPath("$.error.code").value("GONE"));
        }

        @Test
        void updateMeUsesAuthenticatedJwtSubjectAndHandlesMissingUser() throws Exception {
            when(memberService.update(any())).thenReturn(true);

            mockMvc.perform(put("/api/members/me")
                            .principal(jwtPrincipal("ssafy"))
                            .param("name", "SSAFY")
                            .param("email", "ssafy@example.com")
                            .param("password", "new-secret1"))
                    .andExpect(status().isOk());

            ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
            verify(memberService).update(captor.capture());
            assertThat(captor.getValue().userId()).isEqualTo("ssafy");

            when(memberService.findByUserId("ghost")).thenReturn(null);
            mockMvc.perform(get("/api/members/me").principal(jwtPrincipal("ghost")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message").value("User not found"));
        }
    }

    @Nested
    class ExternalAndHealthEndpoints {
        @Test
        void attractionAndChargerControllersTranslateNormalAndExceptionCases() throws Exception {
            mockMvc.perform(post("/api/attractions"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.error.message").value("Use GET /api/attractions"));

            when(attractionService.searchAttractions(new AttractionSearchCondition("1", "", "", "궁", "", "", ""), ""))
                    .thenThrow(new ExternalServiceException(ExternalServiceException.Source.TOUR_API, new RuntimeException("tour failed")));
            mockMvc.perform(get("/api/attractions").param("sidoCode", "1").param("keyword", "궁"))
                    .andExpect(status().isBadGateway())
                    .andExpect(jsonPath("$.error.message").value("Tour API call failed"));

            when(attractionService.searchAttractions(new AttractionSearchCondition("", "", "", "", "126.9", "37.5", ""), ""))
                    .thenThrow(new IllegalStateException("not configured"));
            mockMvc.perform(get("/api/attractions").param("mapX", "126.9").param("mapY", "37.5"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error.message").value("Internal server error"));

            when(chargerService.findChargers("", "", 1, 150))
                    .thenThrow(new ExternalServiceException(ExternalServiceException.Source.EV_CHARGER_API, new RuntimeException("timeout")));
            mockMvc.perform(get("/api/chargers").param("pageNo", "bad").param("numOfRows", "bad"))
                    .andExpect(status().isBadGateway())
                    .andExpect(jsonPath("$.error.message").value("EV charger API call failed"));
        }

        @Test
        void attractionEngagementAndTagEndpointsValidateAndDelegate() throws Exception {
            when(attractionService.existsById(1L)).thenReturn(true);
            when(attractionService.findStats(1L, "ssafy")).thenReturn(new AttractionStats(
                    1L, 2, 4.5, 2, List.of(new AttractionTag(3L, "family")), true, 5
            ));
            when(attractionService.findAllTags()).thenReturn(List.of(new AttractionTag(3L, "family")));
            when(attractionService.replaceTags(1L, List.of(3L))).thenReturn(true);

            mockMvc.perform(put("/api/attractions/1/favorite").principal(jwtPrincipal("ssafy")))
                    .andExpect(status().isOk());
            verify(attractionService).addFavorite(1L, "ssafy");

            mockMvc.perform(put("/api/attractions/1/rating").principal(jwtPrincipal("ssafy")).param("rating", "6"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Rating must be between 1 and 5"));

            mockMvc.perform(get("/api/attractions/1/stats").principal(jwtPrincipal("ssafy")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stats.favoriteCount").value(2))
                    .andExpect(jsonPath("$.data.stats.tags[0].name").value("family"));

            mockMvc.perform(put("/api/attractions/1/tags").principal(jwtPrincipal("ssafy")).param("tagIds", "3"))
                    .andExpect(status().isOk());
            verify(attractionService).replaceTags(1L, List.of(3L));

            mockMvc.perform(get("/api/attraction-tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.tags[0].name").value("family"));
        }

        @Test
        void healthReportsDatabaseStatusAndGlobalHandlerCatchesUnexpectedExceptions() throws Exception {
            when(dbHealthRepository.isConnected()).thenReturn(true);

            mockMvc.perform(get("/api/db/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.db").value("connected"));

            when(dbHealthRepository.isConnected()).thenReturn(false);
            mockMvc.perform(get("/api/db/health"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error.message").value("Database disconnected"));

            mockMvc.perform(get("/test/fail"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"));
        }
    }

    @Test
    void routeEndpointsRejectInvalidInput() throws Exception {
        mockMvc.perform(get("/api/route/optimize").param("points", "37.5|bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Invalid points"));

        mockMvc.perform(get("/api/route/split-by-day").param("points", "37.5|bad").param("days", "two"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Invalid request"));
    }

    @Test
    void controllerContractsUseDtoObjectsInsteadOfRawMaps() throws Exception {
        Path webPackage = Path.of("src/main/java/com/ssafy/enjoytrip/web");
        List<Path> files = Files.walk(webPackage)
                .filter(path -> path.toString().endsWith("Controller.java"))
                .toList();
        for (Path path : files) {
            assertControllerDoesNotUseRawMapContract(path);
        }
    }

    private static JwtAuthenticationToken jwtPrincipal(String userId) {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(userId)
                .claim("name", "SSAFY")
                .claim("email", "ssafy@example.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(7200))
                .build();
        return new JwtAuthenticationToken(jwt);
    }

    private static void assertControllerDoesNotUseRawMapContract(Path path) throws IOException {
        String source = Files.readString(path);
        assertThat(source)
                .as(path.toString())
                .doesNotContain("java.util.Map")
                .doesNotContain("Map.of(")
                .doesNotContain("@RequestParam Map")
                .doesNotContain("@RequestBody Map")
                .doesNotContain("ApiResponse<Map");
    }

    @org.springframework.web.bind.annotation.RestController
    static class FailingController {
        @org.springframework.web.bind.annotation.GetMapping("/test/fail")
        String fail(Principal ignored) {
            throw new IllegalStateException("boom");
        }
    }
}

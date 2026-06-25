package com.ssafy.enjoytrip.core.api.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ssafy.enjoytrip.core.api.web.controller.AttractionController;
import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.service.AttractionService;
import com.ssafy.enjoytrip.core.domain.service.AttractionStatsService;
import com.ssafy.enjoytrip.core.domain.vo.Address;
import com.ssafy.enjoytrip.core.domain.vo.Coordinate;
import com.ssafy.enjoytrip.core.domain.vo.RatingStats;
import java.security.Principal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AttractionControllerTest {
    private AttractionService attractionService;
    private AttractionStatsService attractionStatsService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        attractionService = mock(AttractionService.class);
        attractionStatsService = mock(AttractionStatsService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AttractionController(attractionService, attractionStatsService)
                )
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @DisplayName("인증 사용자는 관광지 추천 목록을 조회한다")
    @Test
    void authenticatedUserGetsAttractionRecommendations() throws Exception {
        when(attractionService.findRecommendations(11L, 10)).thenReturn(List.of(
                attraction(1L, "경복궁"),
                attraction(2L, "남산타워")
        ));

        mockMvc.perform(get("/api/attractions/recommendations")
                        .principal(jwtPrincipal(11L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attractions[0].id").value(1))
                .andExpect(jsonPath("$.data.attractions[0].title").value("경복궁"))
                .andExpect(jsonPath("$.data.attractions[1].id").value(2))
                .andExpect(jsonPath("$.data.attractions[1].title").value("남산타워"));

        verify(attractionService).findRecommendations(11L, 10);
    }

    @DisplayName("limit 파라미터를 지정하면 해당 limit으로 추천을 조회한다")
    @Test
    void attractionRecommendationsRespectsLimitParameter() throws Exception {
        when(attractionService.findRecommendations(11L, 5)).thenReturn(List.of(
                attraction(1L, "경복궁")
        ));

        mockMvc.perform(get("/api/attractions/recommendations")
                        .param("limit", "5")
                        .principal(jwtPrincipal(11L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(attractionService).findRecommendations(11L, 5);
    }

    @DisplayName("미인증 사용자가 관광지 추천을 조회하면 401을 반환한다")
    @Test
    void unauthenticatedUserGetsUnauthorizedForAttractionRecommendations() throws Exception {
        mockMvc.perform(get("/api/attractions/recommendations"))
                .andExpect(status().isUnauthorized());

        verify(attractionService, never()).findRecommendations(null, 10);
    }

    private static Attraction attraction(Long id, String title) {
        return new Attraction(
                id,
                title,
                new Address("서울 종로구", null, null),
                null,
                null,
                null,
                0,
                null,
                null,
                new Coordinate(37.5765, 126.9770),
                null,
                "12",
                null,
                0,
                new RatingStats(0.0, 0),
                false,
                null
        );
    }

    private static Principal jwtPrincipal(long memberId) {
        return () -> String.valueOf(memberId);
    }
}

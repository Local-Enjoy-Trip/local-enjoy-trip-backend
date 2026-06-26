package com.ssafy.enjoytrip.core.api.web;

import com.ssafy.enjoytrip.core.api.web.controller.*;
import com.ssafy.enjoytrip.core.domain.MapCenter;
import com.ssafy.enjoytrip.core.domain.MapExploreResult;
import com.ssafy.enjoytrip.core.domain.NoteMapPin;
import com.ssafy.enjoytrip.core.domain.PlaceMapPin;

import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import com.ssafy.enjoytrip.core.domain.NoteImageUploadUrl;
import com.ssafy.enjoytrip.core.domain.ProfileImageUploadUrl;
import com.ssafy.enjoytrip.core.domain.NoteViewerRelationship;
import com.ssafy.enjoytrip.core.domain.NeighborhoodBriefing;
import com.ssafy.enjoytrip.core.domain.Note;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NoteStatus;
import com.ssafy.enjoytrip.core.domain.NoteVisibility;
import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.query.AttractionSearchCondition;
import com.ssafy.enjoytrip.core.domain.AttractionStats;
import com.ssafy.enjoytrip.core.domain.query.DistanceSearchCondition;
import com.ssafy.enjoytrip.core.domain.vo.Address;
import com.ssafy.enjoytrip.core.domain.vo.Coordinate;
import com.ssafy.enjoytrip.core.domain.vo.RatingStats;
import com.ssafy.enjoytrip.core.domain.vo.TemperatureRange;
import com.ssafy.enjoytrip.core.domain.vo.DateRange;
import com.ssafy.enjoytrip.core.domain.PopularAttractionResult;
import com.ssafy.enjoytrip.core.domain.WeatherForecast;
import com.ssafy.enjoytrip.core.domain.WeatherSummary;
import com.ssafy.enjoytrip.core.domain.WeatherWithForecast;
import com.ssafy.enjoytrip.core.support.error.exception.ExternalServiceException;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.domain.service.DbHealthService;
import com.ssafy.enjoytrip.core.domain.service.AttractionService;
import com.ssafy.enjoytrip.core.domain.service.AttractionStatsService;
import com.ssafy.enjoytrip.core.domain.service.WeatherService;
import com.ssafy.enjoytrip.core.domain.service.MapExploreService;
import com.ssafy.enjoytrip.core.domain.service.MapSearchService;
import com.ssafy.enjoytrip.core.domain.service.MemberService;
import com.ssafy.enjoytrip.core.domain.service.NoteImageUploadService;
import com.ssafy.enjoytrip.core.domain.service.MemberProfileImageService;
import com.ssafy.enjoytrip.core.domain.service.NeighborhoodBriefingService;
import com.ssafy.enjoytrip.core.domain.service.NoteService;
import com.ssafy.enjoytrip.core.support.auth.JwtTokenService;
import com.ssafy.enjoytrip.core.support.auth.OAuthSignupTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_CREDENTIALS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.ATTRACTION_NOT_FOUND;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    private NoteService noteService;
    private MapExploreService mapExploreService;
    private MapSearchService mapSearchService;
    private MemberService memberService;
    private JwtTokenService tokenService;
    private OAuthSignupTicketService oauthSignupTicketService;
    private AttractionService attractionService;
    private AttractionStatsService attractionStatsService;
    private WeatherService weatherService;
    private NeighborhoodBriefingService neighborhoodBriefingService;
    private NoteImageUploadService noteImageUploadService;
    private MemberProfileImageService memberProfileImageService;
    private DbHealthService dbHealthService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        noteService = mock(NoteService.class);
        mapExploreService = mock(MapExploreService.class);
        mapSearchService = mock(MapSearchService.class);
        memberService = mock(MemberService.class);
        tokenService = mock(JwtTokenService.class);
        oauthSignupTicketService = mock(OAuthSignupTicketService.class);
        attractionService = mock(AttractionService.class);
        attractionStatsService = mock(AttractionStatsService.class);
        weatherService = mock(WeatherService.class);
        neighborhoodBriefingService = mock(NeighborhoodBriefingService.class);
        noteImageUploadService = mock(NoteImageUploadService.class);
        memberProfileImageService = mock(MemberProfileImageService.class);
        dbHealthService = mock(DbHealthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new NoteController(noteService),
                        new MemberController(memberService, tokenService, oauthSignupTicketService),
                        new MemberProfileImageController(memberProfileImageService),
                        new AttractionController(attractionService, attractionStatsService),
                        new NeighborhoodBriefingController(neighborhoodBriefingService, weatherService),
                        new MapController(mapExploreService, mapSearchService),
                        new NoteImageController(noteImageUploadService),
                        new HealthController(dbHealthService),
                        new FailingController()
                )
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }



    @Nested
    class NeighborhoodBriefingEndpoints {
        @DisplayName("동네 브리핑을 반환하고 구조화 추천 ID를 노출하지 않는다")
        @Test
        void returnsNeighborhoodBriefingWithoutStructuredRecommendationIds() throws Exception {
            WeatherSummary weather = new WeatherSummary("서울", "맑음", 22, 10, "05:23", "19:33", new TemperatureRange(15, 25));
            List<WeatherForecast> forecasts = List.of(
                    new WeatherForecast("12:00", 22, "맑음", 10),
                    new WeatherForecast("13:00", 23, "맑음", 10),
                    new WeatherForecast("14:00", 24, "맑음", 10),
                    new WeatherForecast("15:00", 25, "구름 많음", 20),
                    new WeatherForecast("16:00", 24, "구름 많음", 20),
                    new WeatherForecast("17:00", 23, "맑음", 10)
            );
            String generatedBriefing = "오늘 서울은 맑고 더운 편이라 "
                    + "한강 저녁 산책 코스 어떠세요?";
            WeatherWithForecast weatherWithForecast = new WeatherWithForecast(weather, forecasts);
            when(weatherService.findWeatherWithForecast(any(), any(), eq("서울"), anyString()))
                    .thenReturn(weatherWithForecast);
            when(neighborhoodBriefingService.brief(eq("서울"), any(WeatherWithForecast.class), anyString()))
                    .thenReturn(new NeighborhoodBriefing(
                            "서울",
                            generatedBriefing,
                            weather,
                            forecasts
                    ));

            mockMvc.perform(get("/api/neighborhood/briefing")
                            .param("regionName", "서울"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.region").value("서울"))
                    .andExpect(jsonPath("$.data.season").doesNotExist())
                    .andExpect(jsonPath("$.data.briefing").isNotEmpty())
                    .andExpect(jsonPath("$.data.courseId").doesNotExist())
                    .andExpect(jsonPath("$.data.courseCandidates").doesNotExist())
                    .andExpect(jsonPath("$.data.recommendations").doesNotExist())
                    .andExpect(jsonPath("$.data.weather.region").value("서울"))
                    .andExpect(jsonPath("$.data.weather.tempMin").value(15))
                    .andExpect(jsonPath("$.data.weather.tempMax").value(25))
                    .andExpect(jsonPath("$.data.forecasts.length()").value(6))
                    .andExpect(jsonPath("$.data.forecasts[0].time").value("12:00"))
                    .andExpect(jsonPath("$.data.forecasts[5].time").value("17:00"));

            verify(neighborhoodBriefingService).brief(eq("서울"), any(WeatherWithForecast.class), anyString());
        }

        @DisplayName("동네 브리핑은 지역 query DTO 검증을 적용한다")
        @Test
        void validatesRegionNameQuery() throws Exception {
            mockMvc.perform(get("/api/neighborhood/briefing"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    class NoteEndpoints {
        @DisplayName("인증 사용자는 JSON 본문으로 쪽지를 생성한다")
        @Test
        void createsNoteWithJsonBodyAndAuthenticatedAuthor() throws Exception {
            Note note = note(1L, 11L, "망원 산책 메모", NoteVisibility.PUBLIC);
            when(noteService.createNote(any())).thenReturn(note);

            mockMvc.perform(post("/api/notes")
                            .principal(jwtPrincipal(11L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title":" 망원 산책 메모 ",
                                      "content":" 오늘 날씨 좋음 ",
                                      "category":"TIP",
                                      "visibility":"PUBLIC",
                                      "latitude":37.5665,
                                      "longitude":126.9780,
                                      "regionName":" 망원동 "
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("망원 산책 메모"))
                    .andExpect(jsonPath("$.data.visibility").value("PUBLIC"));

            ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
            verify(noteService).createNote(captor.capture());
            assertThat(captor.getValue().authorMemberId()).isEqualTo(11L);
            assertThat(captor.getValue().title()).isEqualTo("망원 산책 메모");
            assertThat(captor.getValue().regionName()).isEqualTo("망원동");
        }

        @DisplayName("인증 사용자는 본인 쪽지를 수정하고 삭제한다")
        @Test
        void updatesAndDeletesOwnedNote() throws Exception {
            Note updated = note(1L, 11L, "수정 제목", NoteVisibility.PRIVATE);
            when(noteService.updateNote(any())).thenReturn(updated);

            mockMvc.perform(put("/api/notes/1")
                            .principal(jwtPrincipal(11L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title":"수정 제목",
                                      "content":"수정 내용",
                                      "category":"TIP",
                                      "visibility":"PRIVATE",
                                      "latitude":37.5665,
                                      "longitude":126.9780
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("수정 제목"))
                    .andExpect(jsonPath("$.data.visibility").value("PRIVATE"));

            ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
            verify(noteService).updateNote(captor.capture());
            assertThat(captor.getValue().id()).isEqualTo(1L);
            assertThat(captor.getValue().authorMemberId()).isEqualTo(11L);

            mockMvc.perform(delete("/api/notes/1").principal(jwtPrincipal(11L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
            verify(noteService).deleteNote(1L, 11L);
        }

        @DisplayName("쪽지 저장과 저장 목록은 인증 사용자 기준으로 위임한다")
        @Test
        void noteSaveEndpointsDelegateWithAuthenticatedUser() throws Exception {
            Note note = note(1L, 42L, "저장한 쪽지", NoteVisibility.PUBLIC);
            when(noteService.findSavedNotes(11L, 30)).thenReturn(List.of(note));

            mockMvc.perform(put("/api/notes/1/save").principal(jwtPrincipal(11L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
            verify(noteService).addSave(1L, 11L);

            mockMvc.perform(delete("/api/notes/1/save").principal(jwtPrincipal(11L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
            verify(noteService).removeSave(1L, 11L);

            mockMvc.perform(get("/api/notes/saved")
                            .principal(jwtPrincipal(11L))
                            .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.notes[0].id").value(1))
                    .andExpect(jsonPath("$.data.notes[0].title").value("저장한 쪽지"));
            verify(noteService).findSavedNotes(11L, 30);
        }

        @DisplayName("내가 작성한 쪽지 목록은 인증 사용자 기준으로 위임한다")
        @Test
        void noteMeEndpointsDelegateWithAuthenticatedUser() throws Exception {
            Note note = note(1L, 11L, "내가 작성한 쪽지", NoteVisibility.PUBLIC);
            when(noteService.findWrittenNotes(11L, 30)).thenReturn(List.of(note));

            mockMvc.perform(get("/api/notes/me")
                            .principal(jwtPrincipal(11L))
                            .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.notes[0].id").value(1))
                    .andExpect(jsonPath("$.data.notes[0].title").value("내가 작성한 쪽지"));
            verify(noteService).findWrittenNotes(11L, 30);
        }

        @DisplayName("주변 쪽지는 서울과 500m 기본값으로 조회하고 목록을 반환한다")
        @Test
        void nearbyNotesUseDefaultSeoulAndRadius() throws Exception {
            Note note = note(1L, 42L, "근처 쪽지", NoteVisibility.PUBLIC);
            when(noteService.findNearbyNotes(new DistanceSearchCondition(126.9780, 37.5665, 20, 500.0), null))
                    .thenReturn(List.of(note));

            mockMvc.perform(get("/api/notes/nearby"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.notes[0].id").value(1))
                    .andExpect(jsonPath("$.data.notes[0].title").value("근처 쪽지"))
                    .andExpect(jsonPath("$.data.notes[0].visibility").value("PUBLIC"));

            verify(noteService).findNearbyNotes(
                    new DistanceSearchCondition(126.9780, 37.5665, 20, 500.0),
                    null
            );
        }

        @DisplayName("주변 쪽지는 일부 좌표만 전달되면 검증 오류를 반환한다")
        @Test
        void nearbyNotesRejectPartialCoordinates() throws Exception {
            mockMvc.perform(get("/api/notes/nearby").param("mapY", "37.5665"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message")
                            .value("위도 또는 경도가 유효하지 않습니다."));
        }
    }


    @Nested
    class MapExploreEndpoints {
        @DisplayName("지도 탐색은 일부 좌표만 전달되면 검증 오류를 반환한다")
        @Test
        void mapExploreRejectsPartialCoordinates() throws Exception {
            mockMvc.perform(get("/api/map/explore")
                            .principal(jwtPrincipal(42L))
                            .param("mapX", "126.9780"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message")
                            .value("위도 또는 경도가 유효하지 않습니다."));
        }

        @DisplayName("지도 탐색은 좌표가 없으면 검증 오류를 반환한다")
        @Test
        void mapExploreRequiresCoordinates() throws Exception {
            mockMvc.perform(get("/api/map/explore")
                            .principal(jwtPrincipal(42L)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message")
                            .value("위도 또는 경도가 유효하지 않습니다."));
        }

        @DisplayName("명시 좌표는 서비스 command로 전달된다")
        @Test
        void mapExplorePassesExplicitCoordinates() throws Exception {
            when(mapExploreService.explore(
                    any(),
                    anyDouble(),
                    anyDouble(),
                    anyDouble(),
                    any(),
                    any()
            )).thenReturn(new MapExploreResult(
                    new MapCenter(127.0276, 37.4979, null),
                    750.0,
                    MapExploreFilter.NOTE,
                    List.of(),
                    List.of()
            ));

            mockMvc.perform(get("/api/map/explore")
                            .principal(jwtPrincipal(42L))
                            .param("mapX", "127.0276")
                            .param("mapY", "37.4979")
                            .param("radius", "750")
                            .param("filter", "NOTE")
                            .param("noteCategory", "TIP"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.radiusMeters").value(750.0))
                    .andExpect(jsonPath("$.data.filter").value("NOTE"));

            verify(mapExploreService).explore(
                    42L,
                    127.0276,
                    37.4979,
                    750.0,
                    MapExploreFilter.NOTE,
                    NoteCategory.TIP
            );
        }

        @DisplayName("지도 탐색은 SAVED_PLACE 필터를 서비스로 전달한다")
        @Test
        void mapExplorePassesSavedPlaceFilterToService() throws Exception {
            when(mapExploreService.explore(
                    any(),
                    anyDouble(),
                    anyDouble(),
                    anyDouble(),
                    any(),
                    any()
            )).thenReturn(new MapExploreResult(
                    new MapCenter(126.9780, 37.5665, null),
                    500.0,
                    MapExploreFilter.SAVED_PLACE,
                    List.of(),
                    List.of()
            ));

            mockMvc.perform(get("/api/map/explore")
                            .principal(jwtPrincipal(42L))
                            .param("mapX", "126.9780")
                            .param("mapY", "37.5665")
                            .param("filter", "SAVED_PLACE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.filter").value("SAVED_PLACE"));

            verify(mapExploreService).explore(
                    42L,
                    126.9780,
                    37.5665,
                    500.0,
                    MapExploreFilter.SAVED_PLACE,
                    null
            );
        }

        @DisplayName("지도 탐색은 장소와 SELF/FRIEND/NONE privacy-projected 쪽지를 반환한다")
        @Test
        void mapExploreReturnsPlaceAndPrivacyProjectedNotePins() throws Exception {
            MapExploreResult result = new MapExploreResult(
                    new MapCenter(126.9780, 37.5665, "서울 중구"),
                    1000.0,
                    MapExploreFilter.ALL,
                    List.of(new PlaceMapPin(
                            100L,
                            "서울광장",
                            "서울 중구 세종대로",
                            37.5665,
                            126.9780,
                            "https://example.com/place.jpg",
                            "12",
                            24.5,
                            true,
                            3,
                            4.5,
                            2
                    )),
                    List.of(
                            new NoteMapPin(
                                    1L,
                                    "내 쪽지",
                                    NoteCategory.TIP,
                                    NoteVisibility.PUBLIC,
                                    37.5666,
                                    126.9781,
                                    "서울 중구",
                                    10.0,
                                    "notes/self/sample.jpg",
                                    "https://example.com/notes/self/sample.jpg",
                                    "내닉",
                                    "https://example.com/self.jpg",
                                    NoteViewerRelationship.SELF,
                                    LocalDateTime.of(2026, 6, 15, 10, 0)
                            ),
                            new NoteMapPin(
                                    2L,
                                    "친구 쪽지",
                                    NoteCategory.TIP,
                                    NoteVisibility.FRIENDS,
                                    37.5667,
                                    126.9782,
                                    "서울 중구",
                                    30.0,
                                    "notes/friend/sample.jpg",
                                    "https://example.com/notes/friend/sample.jpg",
                                    "친구닉",
                                    "https://example.com/profile.jpg",
                                    NoteViewerRelationship.FRIEND,
                                    LocalDateTime.of(2026, 6, 15, 10, 1)
                            ),
                            new NoteMapPin(
                                    3L,
                                    "공개 쪽지",
                                    NoteCategory.TIP,
                                    NoteVisibility.PUBLIC,
                                    37.5668,
                                    126.9783,
                                    "서울 중구",
                                    45.0,
                                    null,
                                    null,
                                    "낯선닉",
                                    null,
                                    NoteViewerRelationship.NONE,
                                    LocalDateTime.of(2026, 6, 15, 10, 2)
                            )
                    )
            );
            when(mapExploreService.explore(
                    any(),
                    anyDouble(),
                    anyDouble(),
                    anyDouble(),
                    any(),
                    any()
            )).thenReturn(result);

            mockMvc.perform(get("/api/map/explore")
                            .principal(jwtPrincipal(42L))
                            .param("mapX", "126.9780")
                            .param("mapY", "37.5665"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.places[0].title").value("서울광장"))
                    .andExpect(jsonPath("$.data.notes[0].authorNickname").value("내닉"))
                    .andExpect(jsonPath("$.data.notes[0].authorProfileImageUrl")
                            .value("https://example.com/self.jpg"))
                    .andExpect(jsonPath("$.data.notes[0].relationshipToViewer").value("SELF"))
                    .andExpect(jsonPath("$.data.notes[1].authorNickname").value("친구닉"))
                    .andExpect(jsonPath("$.data.notes[1].authorProfileImageUrl")
                            .value("https://example.com/profile.jpg"))
                    .andExpect(jsonPath("$.data.notes[1].relationshipToViewer").value("FRIEND"))
                    .andExpect(jsonPath("$.data.notes[2].authorNickname").value("낯선닉"))
                    .andExpect(jsonPath("$.data.notes[2].authorProfileImageUrl").doesNotExist())
                    .andExpect(jsonPath("$.data.notes[2].relationshipToViewer").value("NONE"));

            verify(mapExploreService).explore(
                    any(),
                    anyDouble(),
                    anyDouble(),
                    anyDouble(),
                    any(),
                    any()
            );
        }

        @DisplayName("지도 탐색은 5000m를 초과하는 반경도 정상적으로 허용한다")
        @Test
        void mapExploreAllowsRadiusOver5000m() throws Exception {
            when(mapExploreService.explore(
                    any(),
                    anyDouble(),
                    anyDouble(),
                    anyDouble(),
                    any(),
                    any()
            )).thenReturn(new MapExploreResult(
                    new MapCenter(126.9780, 37.5665, null),
                    10000.0,
                    MapExploreFilter.ALL,
                    List.of(),
                    List.of()
            ));

            mockMvc.perform(get("/api/map/explore")
                            .principal(jwtPrincipal(42L))
                            .param("mapX", "126.9780")
                            .param("mapY", "37.5665")
                            .param("radius", "10000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.radiusMeters").value(10000.0));

            verify(mapExploreService).explore(
                    42L,
                    126.9780,
                    37.5665,
                    10000.0,
                    MapExploreFilter.ALL,
                    null
            );
        }
    }


    @Nested
    class MemberProfileImageEndpoints {
        @DisplayName("회원 프로필 이미지 presigned upload는 이미지 타입만 허용한다")
        @Test
        void profileImageUploadValidatesImageContentType() throws Exception {
            mockMvc.perform(post("/api/members/me/profile-image/presigned-upload")
                            .principal(jwtPrincipal(42L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"contentType":"text/plain","fileExtension":"txt"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @DisplayName("회원 프로필 이미지 presigned upload는 profile objectKey와 uploadUrl을 반환한다")
        @Test
        void profileImageUploadReturnsPresignedResponseShape() throws Exception {
            when(memberProfileImageService.createPresignedUpload(any(), any(), any()))
                    .thenReturn(new ProfileImageUploadUrl(
                            "profiles/42/sample.jpg",
                            "http://localhost:9000/dongnepin-notes/profiles/42/sample.jpg?signature=abc",
                            Instant.parse("2026-06-15T01:10:00Z"),
                            "http://localhost:9000/dongnepin-notes/profiles/42/sample.jpg"
                    ));

            mockMvc.perform(post("/api/members/me/profile-image/presigned-upload")
                            .principal(jwtPrincipal(42L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"contentType":"image/jpeg","fileExtension":"jpg"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.objectKey").value("profiles/42/sample.jpg"))
                    .andExpect(jsonPath("$.data.uploadUrl").isNotEmpty())
                    .andExpect(jsonPath("$.data.expiresAt").value("2026-06-15T01:10:00Z"))
                    .andExpect(jsonPath("$.data.publicUrl").isNotEmpty());

            verify(memberProfileImageService).createPresignedUpload(
                    42L,
                    "image/jpeg",
                    "jpg"
            );
        }

        @DisplayName("회원 프로필 이미지 저장은 objectKey와 contentType만 서비스에 전달한다")
        @Test
        void profileImageUpdatePassesObjectKeyOnly() throws Exception {
            mockMvc.perform(put("/api/members/me/profile-image")
                            .principal(jwtPrincipal(42L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "objectKey":"profiles/42/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg",
                                      "contentType":"image/jpeg",
                                      "publicUrl":"https://evil.example.com/sample.jpg"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(memberProfileImageService).updateProfileImage(
                    42L,
                    "profiles/42/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg"
            );
        }

        @DisplayName("회원 프로필 이미지 저장은 이미지 타입만 허용한다")
        @Test
        void profileImageUpdateValidatesImageContentType() throws Exception {
            mockMvc.perform(put("/api/members/me/profile-image")
                            .principal(jwtPrincipal(42L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"objectKey":"profiles/2/sample.txt","contentType":"text/plain"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @DisplayName("회원 프로필 이미지 저장은 다른 사용자 objectKey를 400으로 응답한다")
        @Test
        void profileImageUpdateRejectsForeignObjectKeyAsBadRequest() throws Exception {
            mockMvc.perform(put("/api/members/me/profile-image")
                            .principal(jwtPrincipal(42L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "objectKey":"profiles/other/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg",
                                      "contentType":"image/jpeg"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("C400"));

            verify(memberProfileImageService, never()).updateProfileImage(any(), any());
        }
    }

    @Nested
    class NoteImageEndpoints {
        @DisplayName("쪽지 이미지 presigned upload는 이미지 타입만 허용한다")
        @Test
        void noteImageUploadValidatesImageContentType() throws Exception {
            mockMvc.perform(post("/api/note-images/presigned-upload")
                            .principal(jwtPrincipal(42L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"contentType":"text/plain","fileExtension":"txt"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @DisplayName("쪽지 이미지 presigned upload는 objectKey와 uploadUrl을 반환한다")
        @Test
        void noteImageUploadReturnsPresignedResponseShape() throws Exception {
            when(noteImageUploadService.createPresignedUpload(any(), any(), any()))
                    .thenReturn(new NoteImageUploadUrl(
                    "notes/42/sample.jpg",
                    "http://localhost:9000/dongnepin-notes/notes/42/sample.jpg?signature=abc",
                    Instant.parse("2026-06-15T01:10:00Z"),
                    "http://localhost:9000/dongnepin-notes/notes/42/sample.jpg"
            ));

            mockMvc.perform(post("/api/note-images/presigned-upload")
                            .principal(jwtPrincipal(42L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"contentType":"image/jpeg","fileExtension":"jpg"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.objectKey").value("notes/42/sample.jpg"))
                    .andExpect(jsonPath("$.data.uploadUrl").isNotEmpty())
                    .andExpect(jsonPath("$.data.expiresAt").value("2026-06-15T01:10:00Z"))
                    .andExpect(jsonPath("$.data.publicUrl").isNotEmpty());

            verify(noteImageUploadService).createPresignedUpload(
                    42L,
                    "image/jpeg",
                    "jpg"
            );
        }
    }


    @Nested
    class MemberAuthEndpoints {
        @DisplayName("로그인 실패와 로그아웃 검증을 확인한다")
        @Test
        void loginFailureAndLogoutValidation() throws Exception {
            doThrow(new CoreException(INVALID_CREDENTIALS))
                    .when(memberService)
                    .login("ssafy@example.com", "wrong123");

            mockMvc.perform(post("/api/members/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "ssafy@example.com",
                                      "password": "wrong123"
                                    }
                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.message")
                            .value("아이디 또는 비밀번호가 올바르지 않습니다."));

            mockMvc.perform(post("/api/members/logout"))
                    .andExpect(status().isUnauthorized());
        }

        @DisplayName("내 정보 수정은 인증된 JWT 주체를 사용하고 계정 필드를 변경하지 않는다")
        @Test
        void updateMeUsesAuthenticatedJwtSubjectWithoutAccountFields() throws Exception {
            mockMvc.perform(put("/api/members/me")
                            .principal(jwtPrincipal(11L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "ssafy@example.com",
                                      "password": "new-secret1",
                                      "nickname": "동네핀러"
                                    }
                                    """))
                    .andExpect(status().isOk());

            ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
            verify(memberService).update(captor.capture());
            assertThat(captor.getValue().memberId()).isEqualTo(11L);
            assertThat(captor.getValue().name()).isNull();
            assertThat(captor.getValue().email()).isNull();
            assertThat(captor.getValue().password()).isNull();
            assertThat(captor.getValue().nickname()).isEqualTo("동네핀러");

            when(memberService.findRequiredById(999L)).thenThrow(new CoreException(USER_NOT_FOUND));
            mockMvc.perform(get("/api/members/me").principal(jwtPrincipal(999L)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message").value("사용자를 찾을 수 없습니다."));
        }
    }

    @Nested
    class ExternalAndHealthEndpoints {
        @DisplayName("관광지 컨트롤러는 정상 및 예외 사례를 변환한다")
        @Test
        void attractionControllerTranslatesNormalAndExceptionCases() throws Exception {
            mockMvc.perform(post("/api/attractions"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.error.message").value("GET /api/attractions를 사용하세요."));

            when(attractionService.searchAttractions(
                    new AttractionSearchCondition(1, null, null, "궁", null, null, null),
                    null
            ))
                    .thenThrow(new ExternalServiceException(
                            ExternalServiceException.Source.TOUR_API,
                            new RuntimeException("Tour API 호출에 실패했습니다.")
            ));
            mockMvc.perform(get("/api/attractions").param("sidoCode", "1").param("keyword", "궁"))
                    .andExpect(status().isBadGateway())
                    .andExpect(jsonPath("$.error.message").value("Tour API 호출에 실패했습니다."));

            when(attractionService.searchAttractions(
                    new AttractionSearchCondition(null, null, null, null, 126.9, 37.5, 3000.0),
                    null
            ))
                    .thenThrow(new IllegalStateException("설정되어 있지 않습니다."));
            mockMvc.perform(get("/api/attractions").param("mapX", "126.9").param("mapY", "37.5"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error.message")
                            .value("서버 내부 오류가 발생했습니다."));
        }

        @DisplayName("홈 인기 주변 관광지는 서울과 500m 기본값을 적용하고 전용 인기 수를 반환한다")
        @Test
        void popularNearbyAttractionsApplyHomeDefaultsAndReturnPopularityCount() throws Exception {
            Attraction attraction = new Attraction(
                    1L, "경복궁", new Address("서울 종로구", "", "zip"),
                    "tel", "image", "image2",
                    7, 1, 2, new Coordinate(37.579617, 126.977041),
                    "6", "12", "overview",
                    2, new RatingStats(4.5, 2), true, null
            );
            when(attractionService.findPopularNearbyAttractions(
                    new DistanceSearchCondition(126.9780, 37.5665, 20, 500.0),
                    null
            )).thenReturn(List.of(new PopularAttractionResult(attraction, 120.5, 42L)));

            mockMvc.perform(get("/api/attractions/popular-nearby"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.attractions[0].id").value(1))
                    .andExpect(jsonPath("$.data.attractions[0].title").value("경복궁"))
                    .andExpect(jsonPath("$.data.attractions[0].saveCount").value(2))
                    .andExpect(jsonPath("$.data.attractions[0].saved").value(true))
                    .andExpect(jsonPath("$.data.attractions[0].popularityCount").value(42))
                    .andExpect(jsonPath("$.data.attractions[0].distanceMeters").value(120.5));

            verify(attractionService).findPopularNearbyAttractions(
                    new DistanceSearchCondition(126.9780, 37.5665, 20, 500.0),
                    null
            );
        }

        @DisplayName("홈 인기 주변 관광지는 좌표가 하나만 있으면 검증 오류를 반환한다")
        @Test
        void popularNearbyAttractionsRejectPartialCoordinates() throws Exception {
            mockMvc.perform(get("/api/attractions/popular-nearby").param("mapX", "126.9780"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message")
                            .value("위도 또는 경도가 유효하지 않습니다."));
        }

        @DisplayName("관광지 상세 조회는 단건 장소 상세와 내 참여 상태를 반환한다")
        @Test
        void attractionDetailReturnsSingleAttractionWithViewerState() throws Exception {
            Attraction attraction = new Attraction(
                    1L,
                    "경복궁",
                    new Address("서울 종로구", "", "03045"),
                    "02-3700-3900",
                    "image",
                    "image2",
                    7,
                    1,
                    2,
                    new Coordinate(37.579617, 126.977041),
                    "6",
                    "12",
                    "조선 시대 궁궐입니다.",
                    2,
                    new RatingStats(4.5, 2),
                    true,
                    5
            );
            when(attractionService.findAttractionDetail(1L, 11L)).thenReturn(attraction);

            mockMvc.perform(get("/api/attractions/1").principal(jwtPrincipal(11L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("경복궁"))
                    .andExpect(jsonPath("$.data.imageUrl").value("image"))
                    .andExpect(jsonPath("$.data.overview").value("조선 시대 궁궐입니다."))
                    .andExpect(jsonPath("$.data.saveCount").value(2))
                    .andExpect(jsonPath("$.data.saved").value(true))
                    .andExpect(jsonPath("$.data.myRating").value(5))
                    .andExpect(jsonPath("$.data.attraction").doesNotExist())
                    .andExpect(jsonPath("$.data.firstImage").doesNotExist())
                    .andExpect(jsonPath("$.data.firstImage2").doesNotExist())
                    .andExpect(jsonPath("$.data.sidoCode").doesNotExist())
                    .andExpect(jsonPath("$.data.gugunCode").doesNotExist())
                    .andExpect(jsonPath("$.data.mlevel").doesNotExist());

            verify(attractionService).findAttractionDetail(1L, 11L);
        }

        @DisplayName("관광지 상세 조회는 없는 장소이면 404를 반환한다")
        @Test
        void attractionDetailReturnsNotFoundWhenMissing() throws Exception {
            when(attractionService.findAttractionDetail(999L, null))
                    .thenThrow(new CoreException(ATTRACTION_NOT_FOUND));

            mockMvc.perform(get("/api/attractions/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message").value("관광지를 찾을 수 없습니다."));
        }

        @DisplayName("관광지 참여와 태그 엔드포인트는 검증 후 위임한다")
        @Test
        void attractionEngagementAndTagEndpointsValidateAndDelegate() throws Exception {
            when(attractionStatsService.findStats(1L, 11L)).thenReturn(new AttractionStats(
                    1L, 3, 4.5, 2, true, 5
            ));

            mockMvc.perform(put("/api/attractions/1/save").principal(jwtPrincipal(11L)))
                    .andExpect(status().isOk());
            verify(attractionService).addSave(1L, 11L);

            mockMvc.perform(delete("/api/attractions/1/save").principal(jwtPrincipal(11L)))
                    .andExpect(status().isOk());
            verify(attractionService).removeSave(1L, 11L);

            mockMvc.perform(put("/api/attractions/1/rating")
                            .principal(jwtPrincipal(11L))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"rating":6}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("유효하지 않은 요청입니다."));

            mockMvc.perform(get("/api/attractions/1/stats").principal(jwtPrincipal(11L)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stats.saveCount").value(3))
                    .andExpect(jsonPath("$.data.stats.saved").value(true));
        }

        @DisplayName("헬스 체크는 DB 상태를 보고하고 전역 핸들러는 예상 밖 예외를 잡는다")
        @Test
        void healthReportsDatabaseStatusAndGlobalHandlerCatchesUnexpectedExceptions() throws Exception {
            when(dbHealthService.isConnected()).thenReturn(true);

            mockMvc.perform(get("/api/db/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.db").value("connected"));

            when(dbHealthService.isConnected()).thenReturn(false);
            mockMvc.perform(get("/api/db/health"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error.message")
                            .value("데이터베이스 연결이 끊어졌습니다."));

            mockMvc.perform(get("/test/fail"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error.code").value("I500"));
        }
    }

    @DisplayName("컨트롤러 계약은 원시 Map 대신 DTO를 사용한다")
    @Test
    void controllerContractsUseDtoObjectsInsteadOfRawMaps() throws Exception {
        Path webPackage = Path.of("src/main/java/com/ssafy/enjoytrip/core/api/web");
        List<Path> files = Files.walk(webPackage)
                .filter(path -> path.toString().endsWith("Controller.java"))
                .toList();
        for (Path path : files) {
            assertControllerDoesNotUseRawMapContract(path);
        }
    }

    private static Note note(Long id, Long authorMemberId, String title, NoteVisibility visibility) {
        return new Note(
                id,
                authorMemberId,
                title,
                "content",
                NoteCategory.TIP,
                visibility,
                37.5665,
                126.9780,
                "망원동",
                null,
                null,
                null,
                NoteStatus.ACTIVE,
                LocalDateTime.of(2026, 6, 10, 10, 0),
                null,
                null
        );
    }

    private static JwtAuthenticationToken jwtPrincipal(long memberId) {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(String.valueOf(memberId))
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
                .doesNotContain("java." + "util.Map")
                .doesNotContain("Map.of(")
                .doesNotContain("@RequestParam Map")
                .doesNotContain("@RequestBody Map")
                .doesNotContain("ApiResponse<Map<")
                .doesNotContain("legacyPost")
                .doesNotContain("private static <T> T fail");
        if (!isAdminHtmlController(path, source)) {
            assertThat(MUTATION_MODEL_ATTRIBUTE_PATTERN.matcher(source).find())
                    .as(path.toString())
                    .isFalse();
        }
    }

    private static boolean isAdminHtmlController(Path path, String source) {
        return path.getFileName().toString().startsWith("Admin")
                && source.contains("@Controller")
                && source.contains("/admin/");
    }

    private static final Pattern MUTATION_MODEL_ATTRIBUTE_PATTERN = Pattern.compile(
            "@(?:Post|Put|Patch)Mapping[^\\n]*"
                    + "(?:\\R(?!\\s*@(Get|Delete|Post|Put|Patch)Mapping)[^\\n]*){0,12}"
                    + "@ModelAttribute"
    );

    @RestController
    static class FailingController {
        @GetMapping("/test/fail")
        String fail(Principal ignored) {
            throw new IllegalStateException("처리 중 오류가 발생했습니다.");
        }
    }
}

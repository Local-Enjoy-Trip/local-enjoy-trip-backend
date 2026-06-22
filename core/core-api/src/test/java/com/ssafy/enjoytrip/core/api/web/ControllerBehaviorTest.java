package com.ssafy.enjoytrip.core.api.web;

import com.ssafy.enjoytrip.core.api.web.controller.*;
import com.ssafy.enjoytrip.core.domain.service.MapCenter;
import com.ssafy.enjoytrip.core.domain.service.MapExploreResult;
import com.ssafy.enjoytrip.core.domain.service.NoteMapPin;
import com.ssafy.enjoytrip.core.domain.service.PlaceMapPin;

import com.ssafy.enjoytrip.core.domain.BoardPost;
import com.ssafy.enjoytrip.core.domain.Hotplace;
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
import com.ssafy.enjoytrip.core.domain.PlanItem;
import com.ssafy.enjoytrip.core.domain.TravelPlan;
import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.query.AttractionSearchCondition;
import com.ssafy.enjoytrip.core.domain.AttractionStats;
import com.ssafy.enjoytrip.core.domain.AttractionTag;
import com.ssafy.enjoytrip.core.domain.query.NearbyNotesCondition;
import com.ssafy.enjoytrip.core.domain.query.NearbySearchCondition;
import com.ssafy.enjoytrip.core.domain.service.PopularAttractionResult;
import com.ssafy.enjoytrip.core.domain.WeatherSummary;
import com.ssafy.enjoytrip.core.support.error.exception.ExternalServiceException;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.domain.service.DbHealthService;
import com.ssafy.enjoytrip.core.domain.service.AttractionService;
import com.ssafy.enjoytrip.core.domain.service.AttractionStatsService;
import com.ssafy.enjoytrip.core.domain.service.BoardService;
import com.ssafy.enjoytrip.core.domain.service.EvChargerService;
import com.ssafy.enjoytrip.core.domain.service.HotplaceService;
import com.ssafy.enjoytrip.core.domain.service.JwtTokenService;
import com.ssafy.enjoytrip.core.domain.service.MapExploreService;
import com.ssafy.enjoytrip.core.domain.service.MemberService;
import com.ssafy.enjoytrip.core.domain.service.NoteImageUploadService;
import com.ssafy.enjoytrip.core.domain.service.MemberProfileImageService;
import com.ssafy.enjoytrip.core.domain.service.NeighborhoodBriefingService;
import com.ssafy.enjoytrip.core.domain.service.NoteService;
import com.ssafy.enjoytrip.core.domain.service.NoticeService;
import com.ssafy.enjoytrip.core.domain.service.OAuthSignupTicketService;
import com.ssafy.enjoytrip.core.domain.service.PlanService;
import com.ssafy.enjoytrip.core.domain.service.WeatherService;
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
import static com.ssafy.enjoytrip.core.support.error.ErrorType.PLAN_NOT_FOUND;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.NOTICE_NOT_FOUND;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.HOTPLACE_NOT_FOUND;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.POST_NOT_FOUND;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
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
    private BoardService boardService;
    private HotplaceService hotplaceService;
    private PlanService planService;
    private NoticeService noticeService;
    private NoteService noteService;
    private MapExploreService mapExploreService;
    private MemberService memberService;
    private JwtTokenService tokenService;
    private OAuthSignupTicketService oauthSignupTicketService;
    private AttractionService attractionService;
    private AttractionStatsService attractionStatsService;
    private EvChargerService chargerService;
    private WeatherService weatherService;
    private NeighborhoodBriefingService neighborhoodBriefingService;
    private NoteImageUploadService noteImageUploadService;
    private MemberProfileImageService memberProfileImageService;
    private DbHealthService dbHealthService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        boardService = mock(BoardService.class);
        hotplaceService = mock(HotplaceService.class);
        planService = mock(PlanService.class);
        noticeService = mock(NoticeService.class);
        noteService = mock(NoteService.class);
        mapExploreService = mock(MapExploreService.class);
        memberService = mock(MemberService.class);
        tokenService = mock(JwtTokenService.class);
        oauthSignupTicketService = mock(OAuthSignupTicketService.class);
        attractionService = mock(AttractionService.class);
        attractionStatsService = mock(AttractionStatsService.class);
        chargerService = mock(EvChargerService.class);
        weatherService = mock(WeatherService.class);
        neighborhoodBriefingService = mock(NeighborhoodBriefingService.class);
        noteImageUploadService = mock(NoteImageUploadService.class);
        memberProfileImageService = mock(MemberProfileImageService.class);
        dbHealthService = mock(DbHealthService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new BoardController(boardService),
                        new HotplaceController(hotplaceService),
                        new PlanController(planService),
                        new NoticeController(noticeService),
                        new NoteController(noteService),
                        new MemberController(memberService, tokenService, oauthSignupTicketService),
                        new MemberProfileImageController(memberProfileImageService),
                        new AttractionController(attractionService, attractionStatsService),
                        new AttractionTagController(attractionService),
                        new ChargerController(chargerService),
                        new WeatherController(weatherService),
                        new NeighborhoodBriefingController(neighborhoodBriefingService),
                        new MapController(mapExploreService),
                        new NoteImageController(noteImageUploadService),
                        new HealthController(dbHealthService),
                        new FailingController()
                )
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    class WeatherEndpoints {
        @DisplayName("날씨 브리핑을 반환하고 서비스에 위임한다")
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
    class NeighborhoodBriefingEndpoints {
        @DisplayName("동네 브리핑을 반환하고 구조화 추천 ID를 노출하지 않는다")
        @Test
        void returnsNeighborhoodBriefingWithoutStructuredRecommendationIds() throws Exception {
            when(neighborhoodBriefingService.brief("서울")).thenReturn(new NeighborhoodBriefing(
                    "서울",
                    "오늘 서울은 맑고 더운 편이라 한강 저녁 산책 코스 어떠세요?"
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
                    .andExpect(jsonPath("$.data.recommendations").doesNotExist());

            verify(neighborhoodBriefingService).brief("서울");
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
            Note note = note(1L, "ssafy", "서울 산책 메모", NoteVisibility.PUBLIC);
            when(noteService.createNote(any())).thenReturn(note);

            mockMvc.perform(post("/api/notes")
                            .principal(jwtPrincipal("ssafy"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title":" 서울 산책 메모 ",
                                      "content":" 오늘 날씨 좋음 ",
                                      "category":"TIP",
                                      "visibility":"PUBLIC",
                                      "latitude":37.5665,
                                      "longitude":126.9780,
                                      "regionName":" 서울 "
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("서울 산책 메모"))
                    .andExpect(jsonPath("$.data.visibility").value("PUBLIC"));

            ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
            verify(noteService).createNote(captor.capture());
            assertThat(captor.getValue().authorUserId()).isEqualTo("ssafy");
            assertThat(captor.getValue().title()).isEqualTo("서울 산책 메모");
            assertThat(captor.getValue().regionName()).isEqualTo("서울");
        }

        @DisplayName("인증 사용자는 본인 쪽지를 수정하고 삭제한다")
        @Test
        void updatesAndDeletesOwnedNote() throws Exception {
            Note updated = note(1L, "ssafy", "수정 제목", NoteVisibility.PRIVATE);
            when(noteService.updateNote(any())).thenReturn(updated);

            mockMvc.perform(put("/api/notes/1")
                            .principal(jwtPrincipal("ssafy"))
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
            assertThat(captor.getValue().authorUserId()).isEqualTo("ssafy");

            mockMvc.perform(delete("/api/notes/1").principal(jwtPrincipal("ssafy")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
            verify(noteService).deleteNote(1L, "ssafy");
        }

        @DisplayName("쪽지 저장과 저장 목록은 인증 사용자 기준으로 위임한다")
        @Test
        void noteSaveEndpointsDelegateWithAuthenticatedUser() throws Exception {
            Note note = note(1L, "writer", "저장한 쪽지", NoteVisibility.PUBLIC);
            when(noteService.findSavedNotes("ssafy", 30)).thenReturn(List.of(note));

            mockMvc.perform(put("/api/notes/1/save").principal(jwtPrincipal("ssafy")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
            verify(noteService).addSave(1L, "ssafy");

            mockMvc.perform(delete("/api/notes/1/save").principal(jwtPrincipal("ssafy")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
            verify(noteService).removeSave(1L, "ssafy");

            mockMvc.perform(get("/api/notes/saved")
                            .principal(jwtPrincipal("ssafy"))
                            .param("limit", "30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.notes[0].id").value(1))
                    .andExpect(jsonPath("$.data.notes[0].title").value("저장한 쪽지"));
            verify(noteService).findSavedNotes("ssafy", 30);
        }

        @DisplayName("주변 쪽지는 서울과 500m 기본값으로 조회하고 목록을 반환한다")
        @Test
        void nearbyNotesUseDefaultSeoulAndRadius() throws Exception {
            Note note = note(1L, "writer", "근처 쪽지", NoteVisibility.PUBLIC);
            when(noteService.findNearbyNotes(new NearbyNotesCondition(126.9780, 37.5665, 500.0, 20), null))
                    .thenReturn(List.of(note));

            mockMvc.perform(get("/api/notes/nearby"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.notes[0].id").value(1))
                    .andExpect(jsonPath("$.data.notes[0].title").value("근처 쪽지"))
                    .andExpect(jsonPath("$.data.notes[0].visibility").value("PUBLIC"));

            verify(noteService).findNearbyNotes(new NearbyNotesCondition(126.9780, 37.5665, 500.0, 20), null);
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
                            .principal(jwtPrincipal("viewer"))
                            .param("mapX", "126.9780"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message")
                            .value("위도 또는 경도가 유효하지 않습니다."));
        }

        @DisplayName("지도 탐색은 좌표가 없으면 검증 오류를 반환한다")
        @Test
        void mapExploreRequiresCoordinates() throws Exception {
            mockMvc.perform(get("/api/map/explore")
                            .principal(jwtPrincipal("viewer")))
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
                    anyInt(),
                    any(),
                    any()
            )).thenReturn(new MapExploreResult(
                    new MapCenter(127.0276, 37.4979, null),
                    750.0,
                    10,
                    MapExploreFilter.NOTE,
                    List.of(),
                    List.of()
            ));

            mockMvc.perform(get("/api/map/explore")
                            .principal(jwtPrincipal("viewer"))
                            .param("mapX", "127.0276")
                            .param("mapY", "37.4979")
                            .param("radius", "750")
                            .param("limit", "10")
                            .param("filter", "NOTE")
                            .param("noteCategory", "TIP"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.radiusMeters").value(750.0))
                    .andExpect(jsonPath("$.data.limit").value(10))
                    .andExpect(jsonPath("$.data.filter").value("NOTE"));

            verify(mapExploreService).explore(
                    "viewer",
                    127.0276,
                    37.4979,
                    750.0,
                    10,
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
                    anyInt(),
                    any(),
                    any()
            )).thenReturn(new MapExploreResult(
                    new MapCenter(126.9780, 37.5665, null),
                    500.0,
                    50,
                    MapExploreFilter.SAVED_PLACE,
                    List.of(),
                    List.of()
            ));

            mockMvc.perform(get("/api/map/explore")
                            .principal(jwtPrincipal("viewer"))
                            .param("mapX", "126.9780")
                            .param("mapY", "37.5665")
                            .param("filter", "SAVED_PLACE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.filter").value("SAVED_PLACE"));

            verify(mapExploreService).explore(
                    "viewer",
                    126.9780,
                    37.5665,
                    500.0,
                    50,
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
                    50,
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
                                    "viewer",
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
                                    "friend",
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
                                    "stranger",
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
                    anyInt(),
                    any(),
                    any()
            )).thenReturn(result);

            mockMvc.perform(get("/api/map/explore")
                            .principal(jwtPrincipal("viewer"))
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
                    anyInt(),
                    any(),
                    any()
            );
        }
    }

    @Nested
    class MemberProfileImageEndpoints {
        @DisplayName("회원 프로필 이미지 presigned upload는 이미지 타입만 허용한다")
        @Test
        void profileImageUploadValidatesImageContentType() throws Exception {
            mockMvc.perform(post("/api/members/me/profile-image/presigned-upload")
                            .principal(jwtPrincipal("viewer"))
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
                            "profiles/viewer/sample.jpg",
                            "http://localhost:9000/dongnepin-notes/profiles/viewer/sample.jpg?signature=abc",
                            Instant.parse("2026-06-15T01:10:00Z"),
                            "http://localhost:9000/dongnepin-notes/profiles/viewer/sample.jpg"
                    ));

            mockMvc.perform(post("/api/members/me/profile-image/presigned-upload")
                            .principal(jwtPrincipal("viewer"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"contentType":"image/jpeg","fileExtension":"jpg"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.objectKey").value("profiles/viewer/sample.jpg"))
                    .andExpect(jsonPath("$.data.uploadUrl").isNotEmpty())
                    .andExpect(jsonPath("$.data.expiresAt").value("2026-06-15T01:10:00Z"))
                    .andExpect(jsonPath("$.data.publicUrl").isNotEmpty());

            verify(memberProfileImageService).createPresignedUpload(
                    "viewer",
                    "image/jpeg",
                    "jpg"
            );
        }

        @DisplayName("회원 프로필 이미지 저장은 objectKey와 contentType만 서비스에 전달한다")
        @Test
        void profileImageUpdatePassesObjectKeyOnly() throws Exception {
            mockMvc.perform(put("/api/members/me/profile-image")
                            .principal(jwtPrincipal("viewer"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "objectKey":"profiles/viewer/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg",
                                      "contentType":"image/jpeg",
                                      "publicUrl":"https://evil.example.com/sample.jpg"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(memberProfileImageService).updateProfileImage(
                    "viewer",
                    "profiles/viewer/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg"
            );
        }

        @DisplayName("회원 프로필 이미지 저장은 이미지 타입만 허용한다")
        @Test
        void profileImageUpdateValidatesImageContentType() throws Exception {
            mockMvc.perform(put("/api/members/me/profile-image")
                            .principal(jwtPrincipal("viewer"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"objectKey":"profiles/viewer/sample.txt","contentType":"text/plain"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @DisplayName("회원 프로필 이미지 저장은 다른 사용자 objectKey를 400으로 응답한다")
        @Test
        void profileImageUpdateRejectsForeignObjectKeyAsBadRequest() throws Exception {
            mockMvc.perform(put("/api/members/me/profile-image")
                            .principal(jwtPrincipal("viewer"))
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
                            .principal(jwtPrincipal("viewer"))
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
                    "notes/viewer/sample.jpg",
                    "http://localhost:9000/dongnepin-notes/notes/viewer/sample.jpg?signature=abc",
                    Instant.parse("2026-06-15T01:10:00Z"),
                    "http://localhost:9000/dongnepin-notes/notes/viewer/sample.jpg"
            ));

            mockMvc.perform(post("/api/note-images/presigned-upload")
                            .principal(jwtPrincipal("viewer"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"contentType":"image/jpeg","fileExtension":"jpg"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.objectKey").value("notes/viewer/sample.jpg"))
                    .andExpect(jsonPath("$.data.uploadUrl").isNotEmpty())
                    .andExpect(jsonPath("$.data.expiresAt").value("2026-06-15T01:10:00Z"))
                    .andExpect(jsonPath("$.data.publicUrl").isNotEmpty());

            verify(noteImageUploadService).createPresignedUpload(
                    "viewer",
                    "image/jpeg",
                    "jpg"
            );
        }
    }

    @Nested
    class BoardEndpoints {
        @DisplayName("게시글 생성은 값을 정리해 서비스에 전달한다")
        @Test
        void createTrimsAndPassesBoardPostToService() throws Exception {
            mockMvc.perform(post("/api/boards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "id":" b1 ",
                                      "title":" title ",
                                      "content":" content ",
                                      "author":" ssafy "
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));

            ArgumentCaptor<BoardPost> captor = ArgumentCaptor.forClass(BoardPost.class);
            verify(boardService).insertPost(captor.capture());
            assertThat(captor.getValue()).isEqualTo(new BoardPost("b1", "title", "content", "ssafy", "", ""));
        }

        @DisplayName("검증 실패와 서비스 예외 응답을 보고한다")
        @Test
        void reportsValidationNotFoundAndServiceExceptionCases() throws Exception {
            mockMvc.perform(post("/api/boards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"id":"b1"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("C400"));

            doThrow(new CoreException(POST_NOT_FOUND)).when(boardService).updatePost(any());
            mockMvc.perform(put("/api/boards/b1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title":"title","content":"content"}
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message").value("게시글을 찾을 수 없습니다."));

            doThrow(new IllegalStateException("쓰기 작업에 실패했습니다."))
                    .when(boardService)
                    .insertPost(any());
            mockMvc.perform(post("/api/boards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"id":"b2","title":"title","content":"content","author":"ssafy"}
                                    """))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error.code").value("I500"));
        }
    }

    @Nested
    class HotplaceEndpoints {
        @DisplayName("사용자별 핫플레이스를 조회하고 좌표로 생성한다")
        @Test
        void findsHotplacesByUserAndCreatesWithCoordinates() throws Exception {
            when(hotplaceService.findHotplacesByUser("ssafy")).thenReturn(List.of(
                    new Hotplace(
                            "h1", "ssafy", "남산", "view", "2026-05-14", 37.55, 126.99, "night", "",
                            "created"
                    )
            ));

            mockMvc.perform(get("/api/hotplaces").param("userId", " ssafy "))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.hotplaces[0].id").value("h1"));

            mockMvc.perform(post("/api/hotplaces")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "id":"h2",
                                      "userId":"ssafy",
                                      "title":"광안리",
                                      "type":"beach",
                                      "visitDate":"2026-05-15",
                                      "lat":35.153,
                                      "lng":129.118,
                                      "description":"sea"
                                    }
                                    """))
                    .andExpect(status().isCreated());

            ArgumentCaptor<Hotplace> captor = ArgumentCaptor.forClass(Hotplace.class);
            verify(hotplaceService).insertHotplace(captor.capture());
            assertThat(captor.getValue().lat()).isEqualTo(35.153);
            assertThat(captor.getValue().lng()).isEqualTo(129.118);
        }

        @DisplayName("잘못된 좌표와 누락된 삭제 대상을 거부한다")
        @Test
        void rejectsInvalidCoordinatesAndMissingDeleteTarget() throws Exception {
            mockMvc.perform(post("/api/hotplaces")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "id":"h1",
                                      "userId":"ssafy",
                                      "title":"남산",
                                      "type":"view",
                                      "visitDate":"2026-05-14",
                                      "lat":100.0,
                                      "lng":126.99
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("유효하지 않은 요청입니다."));

            doThrow(new CoreException(HOTPLACE_NOT_FOUND))
                    .when(hotplaceService)
                    .deleteHotplaceOrThrow("h-missing");
            mockMvc.perform(delete("/api/hotplaces/h-missing"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message")
                            .value("핫플레이스를 찾을 수 없습니다."));
        }
    }

    @Nested
    class PlanAndNoticeEndpoints {
        @DisplayName("계획 조회는 정규화된 경로 항목만 반환하고 저장 JSON 대체 파싱을 하지 않는다")
        @Test
        void planFindReturnsOnlyNormalizedRouteItemsAndDoesNotParseStoredJsonFallback() throws Exception {
            when(planService.findPlansByUser("ssafy")).thenReturn(List.of(
                    new TravelPlan(
                            "p1", "ssafy", "서울", "2026-05-14", "2026-05-15", 1000, null,
                            "[{\"title\":\"A\"}]", "created"
                    ),
                    new TravelPlan(
                            "p2", "ssafy", "부산", "2026-05-16", "2026-05-17", 2000, "note",
                            "not json", "created"
                    )
            ));

            mockMvc.perform(get("/api/plans").param("userId", " ssafy "))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.plans[0].routeItems", empty()))
                    .andExpect(jsonPath("$.data.plans[0].note").value(""))
                    .andExpect(jsonPath("$.data.plans[1].routeItems", empty()));
        }

        @DisplayName("없는 계획 삭제는 찾을 수 없음으로 응답한다")
        @Test
        void planDeleteMissingReturnsNotFound() throws Exception {
            doThrow(new CoreException(PLAN_NOT_FOUND)).when(planService).deletePlan("ssafy", "missing");

            mockMvc.perform(delete("/api/plans/missing").principal(jwtPrincipal("ssafy")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message").value("여행 계획을 찾을 수 없습니다."));
        }

        @DisplayName("표준 계획 생성은 인증 사용자 기준의 타입화된 경로 항목을 저장한다")
        @Test
        void canonicalPlanCreateStoresTypedRouteItemsFromAuthenticatedUser() throws Exception {
            mockMvc.perform(post("/api/plans")
                            .contentType(MediaType.APPLICATION_JSON)
                            .principal(jwtPrincipal("ssafy"))
                            .content("""
                                    {
                                      "id":"p-route",
                                      "title":"서울",
                                      "startDate":"2026-05-14",
                                      "endDate":"2026-05-15",
                                      "routeItems":[
                                        {"attractionId":10,"day":2,"memo":"lunch","stayMinutes":120},
                                        {"attractionId":11}
                                      ]
                                    }
                                    """))
                    .andExpect(status().isOk());

            ArgumentCaptor<TravelPlan> planCaptor = ArgumentCaptor.forClass(TravelPlan.class);
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<PlanItem>> itemCaptor = ArgumentCaptor.forClass(List.class);
            verify(planService).createPlan(planCaptor.capture(), itemCaptor.capture());

            assertThat(planCaptor.getValue().id()).isEqualTo("p-route");
            assertThat(planCaptor.getValue().userId()).isEqualTo("ssafy");
            assertThat(itemCaptor.getValue())
                    .extracting("attractionId")
                    .containsExactly(10L, 11L);
            assertThat(itemCaptor.getValue().getFirst().day()).isEqualTo(2);
            assertThat(itemCaptor.getValue().getFirst().memo()).isEqualTo("lunch");
        }

        @DisplayName("표준 계획 생성 검증 실패는 표준 잘못된 요청 응답을 사용한다")
        @Test
        void canonicalPlanCreateValidationFailureUsesStandardBadRequestEnvelope() throws Exception {
            mockMvc.perform(post("/api/plans")
                            .contentType(MediaType.APPLICATION_JSON)
                            .principal(jwtPrincipal("ssafy"))
                            .content("""
                                    {
                                      "id":"",
                                      "title":"서울",
                                      "startDate":"2026-05-14",
                                      "endDate":"2026-05-15",
                                      "budget":-1
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("유효하지 않은 요청입니다."));
        }

        @DisplayName("표준 계획 수정은 경로 항목 필드가 없으면 기존 항목을 유지한다")
        @Test
        void canonicalPlanUpdateKeepsRouteItemsWhenRouteItemsFieldIsAbsent() throws Exception {
            mockMvc.perform(put("/api/plans/p-route")
                            .contentType(MediaType.APPLICATION_JSON)
                            .principal(jwtPrincipal("ssafy"))
                            .content("""
                                    {
                                      "title":"서울 수정",
                                      "budget":2000
                                    }
                                    """))
                    .andExpect(status().isOk());

            verify(planService).updatePlan(
                    "ssafy",
                    "p-route",
                    "서울 수정",
                    null,
                    null,
                    2000,
                    null,
                    null
            );
        }

        @DisplayName("공지 생성과 수정 및 삭제의 검증 사례를 확인한다")
        @Test
        void noticeCreateUpdateAndDeleteValidationCases() throws Exception {
            mockMvc.perform(post("/api/notices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title":"공지","content":"내용","author":"admin"}
                                    """))
                    .andExpect(status().isCreated());
            verify(noticeService).insertNotice(any());

            mockMvc.perform(put("/api/notices/0")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title":"공지","content":"내용"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("유효하지 않은 id입니다."));

            doThrow(new CoreException(NOTICE_NOT_FOUND)).when(noticeService).updateNoticeOrThrow(any());
            mockMvc.perform(put("/api/notices/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title":"공지","content":"내용"}
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message").value("공지사항을 찾을 수 없습니다."));
        }
    }

    @Nested
    class MemberAuthEndpoints {
        @DisplayName("로그인 실패와 로그아웃 검증을 확인한다")
        @Test
        void loginFailureAndLogoutValidation() throws Exception {
            doThrow(new CoreException(INVALID_CREDENTIALS)).when(memberService).login("ssafy", "wrong");

            mockMvc.perform(post("/api/members/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "userId": "ssafy",
                                      "password": "wrong"
                                    }
                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.message")
                            .value("아이디 또는 비밀번호가 올바르지 않습니다."));

            mockMvc.perform(post("/api/members/logout"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("유효하지 않은 요청입니다."));
        }

        @DisplayName("내 정보 수정은 인증된 JWT 주체를 사용하고 계정 필드를 변경하지 않는다")
        @Test
        void updateMeUsesAuthenticatedJwtSubjectWithoutAccountFields() throws Exception {
            mockMvc.perform(put("/api/members/me")
                            .principal(jwtPrincipal("ssafy"))
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
            assertThat(captor.getValue().userId()).isEqualTo("ssafy");
            assertThat(captor.getValue().name()).isNull();
            assertThat(captor.getValue().email()).isNull();
            assertThat(captor.getValue().password()).isNull();
            assertThat(captor.getValue().nickname()).isEqualTo("동네핀러");

            when(memberService.findRequiredByUserId("ghost")).thenThrow(new CoreException(USER_NOT_FOUND));
            mockMvc.perform(get("/api/members/me").principal(jwtPrincipal("ghost")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message").value("사용자를 찾을 수 없습니다."));
        }
    }

    @Nested
    class ExternalAndHealthEndpoints {
        @DisplayName("관광지와 충전소 컨트롤러는 정상 및 예외 사례를 변환한다")
        @Test
        void attractionAndChargerControllersTranslateNormalAndExceptionCases() throws Exception {
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

            mockMvc.perform(get("/api/chargers").param("pageNo", "bad").param("numOfRows", "bad"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("유효하지 않은 요청입니다."));

            when(chargerService.findChargers(null, null, 1, 150))
                    .thenThrow(new ExternalServiceException(
                            ExternalServiceException.Source.EV_CHARGER_API,
                            new RuntimeException("요청 시간이 초과되었습니다.")
                    ));
            mockMvc.perform(get("/api/chargers"))
                    .andExpect(status().isBadGateway())
                    .andExpect(jsonPath("$.error.message")
                            .value("EV 충전기 API 호출에 실패했습니다."));
        }

        @DisplayName("홈 인기 주변 관광지는 서울과 500m 기본값을 적용하고 전용 인기 수를 반환한다")
        @Test
        void popularNearbyAttractionsApplyHomeDefaultsAndReturnPopularityCount() throws Exception {
            Attraction attraction = new Attraction(
                    1L, "경복궁", "서울 종로구", "", "zip", "tel", "image", "image2",
                    7, 1, 2, 37.579617, 126.977041, "6", "12", "overview",
                    4, 2, 4.5, 2, List.of(), false, true, null
            );
            when(attractionService.findPopularNearbyAttractions(
                    new NearbySearchCondition(126.9780, 37.5665, 500.0, 20),
                    null
            )).thenReturn(List.of(new PopularAttractionResult(attraction, 120.5, 42L)));

            mockMvc.perform(get("/api/attractions/popular-nearby"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.attractions[0].id").value(1))
                    .andExpect(jsonPath("$.data.attractions[0].title").value("경복궁"))
                    .andExpect(jsonPath("$.data.attractions[0].favoriteCount").value(4))
                    .andExpect(jsonPath("$.data.attractions[0].saveCount").value(2))
                    .andExpect(jsonPath("$.data.attractions[0].saved").value(true))
                    .andExpect(jsonPath("$.data.attractions[0].popularityCount").value(42))
                    .andExpect(jsonPath("$.data.attractions[0].distanceMeters").value(120.5));

            verify(attractionService).findPopularNearbyAttractions(
                    new NearbySearchCondition(126.9780, 37.5665, 500.0, 20),
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

        @DisplayName("관광지 참여와 태그 엔드포인트는 검증 후 위임한다")
        @Test
        void attractionEngagementAndTagEndpointsValidateAndDelegate() throws Exception {
            when(attractionStatsService.findStats(1L, "ssafy")).thenReturn(new AttractionStats(
                    1L, 2, 3, 4.5, 2, List.of(new AttractionTag(3L, "family")), true, true, 5
            ));
            when(attractionService.findAllTags()).thenReturn(List.of(new AttractionTag(3L, "family")));
            when(attractionService.replaceTags(1L, List.of(3L))).thenReturn(true);

            mockMvc.perform(put("/api/attractions/1/favorite").principal(jwtPrincipal("ssafy")))
                    .andExpect(status().isOk());
            verify(attractionService).addFavorite(1L, "ssafy");

            mockMvc.perform(put("/api/attractions/1/save").principal(jwtPrincipal("ssafy")))
                    .andExpect(status().isOk());
            verify(attractionService).addSave(1L, "ssafy");

            mockMvc.perform(delete("/api/attractions/1/save").principal(jwtPrincipal("ssafy")))
                    .andExpect(status().isOk());
            verify(attractionService).removeSave(1L, "ssafy");

            mockMvc.perform(put("/api/attractions/1/rating")
                            .principal(jwtPrincipal("ssafy"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"rating":6}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("유효하지 않은 요청입니다."));

            mockMvc.perform(get("/api/attractions/1/stats").principal(jwtPrincipal("ssafy")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stats.favoriteCount").value(2))
                    .andExpect(jsonPath("$.data.stats.saveCount").value(3))
                    .andExpect(jsonPath("$.data.stats.saved").value(true))
                    .andExpect(jsonPath("$.data.stats.tags[0].name").value("family"));

            mockMvc.perform(put("/api/attractions/1/tags")
                            .principal(jwtPrincipal("ssafy"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"tagIds":[3]}
                                    """))
                    .andExpect(status().isOk());
            verify(attractionService).replaceTagsOrThrow(1L, List.of(3L));

            mockMvc.perform(get("/api/attraction-tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.tags[0].name").value("family"));
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

    private static Note note(Long id, String authorUserId, String title, NoteVisibility visibility) {
        return new Note(
                id,
                authorUserId,
                title,
                "content",
                NoteCategory.TIP,
                visibility,
                37.5665,
                126.9780,
                "서울",
                null,
                null,
                null,
                NoteStatus.ACTIVE,
                LocalDateTime.of(2026, 6, 10, 10, 0),
                null,
                null
        );
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
                .doesNotContain("java." + "util.Map")
                .doesNotContain("Map.of(")
                .doesNotContain("@RequestParam Map")
                .doesNotContain("@RequestBody Map")
                .doesNotContain("ApiResponse<Map<")
                .doesNotContain("legacyPost")
                .doesNotContain("private static <T> T fail");
        assertThat(MUTATION_MODEL_ATTRIBUTE_PATTERN.matcher(source).find())
                .as(path.toString())
                .isFalse();
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

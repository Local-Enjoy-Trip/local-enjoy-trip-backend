package com.ssafy.enjoytrip.web;
import com.ssafy.enjoytrip.web.controller.*;

import com.ssafy.enjoytrip.domain.BoardPost;
import com.ssafy.enjoytrip.domain.Hotplace;
import com.ssafy.enjoytrip.dto.query.MapExploreCommand;
import com.ssafy.enjoytrip.domain.MapExploreResult;
import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.domain.MapCenter;
import com.ssafy.enjoytrip.domain.MapExploreFilter;
import com.ssafy.enjoytrip.dto.command.NoteImageUploadCommand;
import com.ssafy.enjoytrip.domain.NoteImageUploadUrl;
import com.ssafy.enjoytrip.domain.NoteMapPin;
import com.ssafy.enjoytrip.domain.NoteViewerRelationship;
import com.ssafy.enjoytrip.domain.PlaceMapPin;
import com.ssafy.enjoytrip.domain.NeighborhoodBriefing;
import com.ssafy.enjoytrip.domain.Note;
import com.ssafy.enjoytrip.domain.NoteCategory;
import com.ssafy.enjoytrip.domain.NoteStatus;
import com.ssafy.enjoytrip.domain.NoteVisibility;
import com.ssafy.enjoytrip.domain.TravelPlan;
import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.dto.query.AttractionSearchCondition;
import com.ssafy.enjoytrip.domain.AttractionStats;
import com.ssafy.enjoytrip.domain.AttractionTag;
import com.ssafy.enjoytrip.dto.command.CreateNoteCommand;
import com.ssafy.enjoytrip.dto.query.NearbyNotesCondition;
import com.ssafy.enjoytrip.dto.query.NearbySearchCondition;
import com.ssafy.enjoytrip.domain.PopularAttraction;
import com.ssafy.enjoytrip.dto.command.UpdateNoteCommand;
import com.ssafy.enjoytrip.domain.WeatherSummary;
import com.ssafy.enjoytrip.exception.ExternalServiceException;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.repository.DbHealthRepository;
import com.ssafy.enjoytrip.service.AttractionService;
import com.ssafy.enjoytrip.service.BoardService;
import com.ssafy.enjoytrip.service.EvChargerService;
import com.ssafy.enjoytrip.service.HotplaceService;
import com.ssafy.enjoytrip.service.JwtTokenService;
import com.ssafy.enjoytrip.service.MapExploreService;
import com.ssafy.enjoytrip.service.MemberService;
import com.ssafy.enjoytrip.service.NoteImageUploadService;
import com.ssafy.enjoytrip.service.NeighborhoodBriefingService;
import com.ssafy.enjoytrip.service.NoteService;
import com.ssafy.enjoytrip.service.NoticeService;
import com.ssafy.enjoytrip.service.OAuthSignupTicketService;
import com.ssafy.enjoytrip.service.PlanService;
import com.ssafy.enjoytrip.dto.command.PlanMutationCommand;
import com.ssafy.enjoytrip.service.RouteOptimizationService;
import com.ssafy.enjoytrip.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.ssafy.enjoytrip.web.mapper.PlanResponseAssembler;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_CREDENTIALS;
import static com.ssafy.enjoytrip.support.error.ErrorType.MEMBER_REPRESENTATIVE_LOCATION_REQUIRED;
import static com.ssafy.enjoytrip.support.error.ErrorType.PLAN_NOT_FOUND;
import static com.ssafy.enjoytrip.support.error.ErrorType.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private NoteService noteService;
    private MapExploreService mapExploreService;
    private MemberService memberService;
    private JwtTokenService tokenService;
    private OAuthSignupTicketService oauthSignupTicketService;
    private AttractionService attractionService;
    private EvChargerService chargerService;
    private WeatherService weatherService;
    private NeighborhoodBriefingService neighborhoodBriefingService;
    private NoteImageUploadService noteImageUploadService;
    private DbHealthRepository dbHealthRepository;
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
        chargerService = mock(EvChargerService.class);
        weatherService = mock(WeatherService.class);
        neighborhoodBriefingService = mock(NeighborhoodBriefingService.class);
        noteImageUploadService = mock(NoteImageUploadService.class);
        dbHealthRepository = mock(DbHealthRepository.class);

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new BoardController(boardService),
                        new HotplaceController(hotplaceService),
                        new PlanController(
                                planService,
                                new PlanResponseAssembler(planService)
                        ),
                        new NoticeController(noticeService),
                        new NoteController(noteService),
                        new MemberController(memberService, tokenService, oauthSignupTicketService),
                        new AttractionController(attractionService),
                        new AttractionTagController(attractionService),
                        new ChargerController(chargerService),
                        new WeatherController(weatherService),
                        new NeighborhoodBriefingController(neighborhoodBriefingService),
                        new MapController(mapExploreService),
                        new NoteImageController(noteImageUploadService),
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

            ArgumentCaptor<CreateNoteCommand> captor = ArgumentCaptor.forClass(CreateNoteCommand.class);
            verify(noteService).createNote(captor.capture());
            assertThat(captor.getValue().authorUserId()).isEqualTo("ssafy");
            assertThat(captor.getValue().title()).isEqualTo("서울 산책 메모");
            assertThat(captor.getValue().regionName()).isEqualTo("서울");
        }

        @DisplayName("쪽지 생성은 인증이 없으면 거부한다")
        @Test
        void rejectsCreateWithoutAuthentication() throws Exception {
            mockMvc.perform(post("/api/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title":"제목",
                                      "content":"내용",
                                      "category":"TIP",
                                      "visibility":"PUBLIC",
                                      "latitude":37.5665,
                                      "longitude":126.9780
                                    }
                                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.message").value("인증이 필요합니다."));
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

            ArgumentCaptor<UpdateNoteCommand> captor = ArgumentCaptor.forClass(UpdateNoteCommand.class);
            verify(noteService).updateNote(captor.capture());
            assertThat(captor.getValue().id()).isEqualTo(1L);
            assertThat(captor.getValue().authorUserId()).isEqualTo("ssafy");

            mockMvc.perform(delete("/api/notes/1").principal(jwtPrincipal("ssafy")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
            verify(noteService).deleteNote(1L, "ssafy");
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
        @DisplayName("지도 탐색은 인증이 필요하다")
        @Test
        void mapExploreRequiresAuthentication() throws Exception {
            mockMvc.perform(get("/api/map/explore"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        }

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

        @DisplayName("대표 동네가 없고 좌표도 없으면 400을 반환한다")
        @Test
        void mapExploreRequiresRepresentativeLocationWhenCoordinatesAreMissing() throws Exception {
            when(mapExploreService.explore(any()))
                    .thenThrow(new CoreException(MEMBER_REPRESENTATIVE_LOCATION_REQUIRED));

            mockMvc.perform(get("/api/map/explore")
                            .principal(jwtPrincipal("viewer")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message")
                            .value("대표 동네 위치를 먼저 설정하세요."));
        }

        @DisplayName("명시 좌표는 대표 위치 fallback보다 우선하여 서비스 command로 전달된다")
        @Test
        void mapExploreExplicitCoordinatesOverrideRepresentativeFallback() throws Exception {
            when(mapExploreService.explore(any())).thenReturn(new MapExploreResult(
                    new MapCenter(127.0276, 37.4979, null, false),
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
                    .andExpect(jsonPath("$.data.center.fromRepresentativeLocation").value(false))
                    .andExpect(jsonPath("$.data.radiusMeters").value(750.0))
                    .andExpect(jsonPath("$.data.limit").value(10))
                    .andExpect(jsonPath("$.data.filter").value("NOTE"));

            ArgumentCaptor<MapExploreCommand> captor = ArgumentCaptor.forClass(MapExploreCommand.class);
            verify(mapExploreService).explore(captor.capture());
            assertThat(captor.getValue().viewerUserId()).isEqualTo("viewer");
            assertThat(captor.getValue().longitude()).isEqualTo(127.0276);
            assertThat(captor.getValue().latitude()).isEqualTo(37.4979);
            assertThat(captor.getValue().radiusMeters()).isEqualTo(750.0);
            assertThat(captor.getValue().limit()).isEqualTo(10);
            assertThat(captor.getValue().filter()).isEqualTo(MapExploreFilter.NOTE);
            assertThat(captor.getValue().noteCategory()).isEqualTo(NoteCategory.TIP);
        }

        @DisplayName("지도 탐색은 장소와 SELF/FRIEND/NONE privacy-projected 쪽지를 반환한다")
        @Test
        void mapExploreReturnsPlaceAndPrivacyProjectedNotePins() throws Exception {
            MapExploreResult result = new MapExploreResult(
                    new MapCenter(126.9780, 37.5665, "서울 중구", true),
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
            when(mapExploreService.explore(any())).thenReturn(result);

            mockMvc.perform(get("/api/map/explore")
                            .principal(jwtPrincipal("viewer")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.center.fromRepresentativeLocation").value(true))
                    .andExpect(jsonPath("$.data.places[0].title").value("서울광장"))
                    .andExpect(jsonPath("$.data.notes[0].authorNickname").value("내닉"))
                    .andExpect(jsonPath("$.data.notes[0].authorProfileImageUrl").value("https://example.com/self.jpg"))
                    .andExpect(jsonPath("$.data.notes[0].relationshipToViewer").value("SELF"))
                    .andExpect(jsonPath("$.data.notes[1].authorNickname").value("친구닉"))
                    .andExpect(jsonPath("$.data.notes[1].authorProfileImageUrl")
                            .value("https://example.com/profile.jpg"))
                    .andExpect(jsonPath("$.data.notes[1].relationshipToViewer").value("FRIEND"))
                    .andExpect(jsonPath("$.data.notes[2].authorNickname").value("낯선닉"))
                    .andExpect(jsonPath("$.data.notes[2].authorProfileImageUrl").doesNotExist())
                    .andExpect(jsonPath("$.data.notes[2].relationshipToViewer").value("NONE"));

            verify(mapExploreService).explore(any());
        }
    }

    @Nested
    class NoteImageEndpoints {
        @DisplayName("쪽지 이미지 presigned upload는 인증이 필요하다")
        @Test
        void noteImageUploadRequiresAuthentication() throws Exception {
            mockMvc.perform(post("/api/note-images/presigned-upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"contentType":"image/jpeg","fileExtension":"jpg"}
                                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        }

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
            when(noteImageUploadService.createPresignedUpload(any())).thenReturn(new NoteImageUploadUrl(
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

            ArgumentCaptor<NoteImageUploadCommand> captor = ArgumentCaptor.forClass(NoteImageUploadCommand.class);
            verify(noteImageUploadService).createPresignedUpload(captor.capture());
            assertThat(captor.getValue().userId()).isEqualTo("viewer");
            assertThat(captor.getValue().contentType()).isEqualTo("image/jpeg");
            assertThat(captor.getValue().fileExtension()).isEqualTo("jpg");
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
                    .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

            when(boardService.updatePost(any())).thenReturn(false);
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
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"));
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

            when(hotplaceService.deleteHotplace("h-missing")).thenReturn(false);
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

            ArgumentCaptor<PlanMutationCommand> commandCaptor =
                    ArgumentCaptor.forClass(PlanMutationCommand.class);
            verify(planService).createPlan(eq("ssafy"), commandCaptor.capture());

            assertThat(commandCaptor.getValue().id()).isEqualTo("p-route");
            assertThat(commandCaptor.getValue().routeItems())
                    .extracting("attractionId")
                    .containsExactly(10L, 11L);
            assertThat(commandCaptor.getValue().routeItems().getFirst().day()).isEqualTo(2);
            assertThat(commandCaptor.getValue().routeItems().getFirst().memo()).isEqualTo("lunch");
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

            ArgumentCaptor<PlanMutationCommand> commandCaptor =
                    ArgumentCaptor.forClass(PlanMutationCommand.class);
            verify(planService).updatePlan(eq("ssafy"), eq("p-route"), commandCaptor.capture());

            assertThat(commandCaptor.getValue().title()).isEqualTo("서울 수정");
            assertThat(commandCaptor.getValue().budget()).isEqualTo(2000);
            assertThat(commandCaptor.getValue().routeItems()).isNull();
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

            when(noticeService.updateNotice(any())).thenReturn(false);
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
        @DisplayName("로그인 실패와 로그아웃 검증 및 비밀번호 조회 종료를 확인한다")
        @Test
        void loginFailureLogoutValidationAndPasswordLookupGone() throws Exception {
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

            mockMvc.perform(post("/api/members/password-lookup-requests")
                            .param("userId", "ssafy")
                            .param("email", "ssafy@example.com"))
                    .andExpect(status().isGone())
                    .andExpect(jsonPath("$.error.code").value("GONE"));
        }

        @DisplayName("내 정보 수정은 인증된 JWT 주체를 사용하고 없는 사용자를 처리한다")
        @Test
        void updateMeUsesAuthenticatedJwtSubjectAndHandlesMissingUser() throws Exception {
            mockMvc.perform(put("/api/members/me")
                            .principal(jwtPrincipal("ssafy"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "SSAFY",
                                      "email": "ssafy@example.com",
                                      "password": "new-secret1"
                                    }
                                    """))
                    .andExpect(status().isOk());

            ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
            verify(memberService).update(captor.capture());
            assertThat(captor.getValue().userId()).isEqualTo("ssafy");

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
                    new AttractionSearchCondition("1", "", "", "궁", "", "", ""),
                    ""
            ))
                    .thenThrow(new ExternalServiceException(
                            ExternalServiceException.Source.TOUR_API,
                            new RuntimeException("Tour API 호출에 실패했습니다.")
                    ));
            mockMvc.perform(get("/api/attractions").param("sidoCode", "1").param("keyword", "궁"))
                    .andExpect(status().isBadGateway())
                    .andExpect(jsonPath("$.error.message").value("Tour API 호출에 실패했습니다."));

            when(attractionService.searchAttractions(
                    new AttractionSearchCondition("", "", "", "", "126.9", "37.5", ""),
                    ""
            ))
                    .thenThrow(new IllegalStateException("설정되어 있지 않습니다."));
            mockMvc.perform(get("/api/attractions").param("mapX", "126.9").param("mapY", "37.5"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error.message")
                            .value("서버 내부 오류가 발생했습니다."));

            mockMvc.perform(get("/api/chargers").param("pageNo", "bad").param("numOfRows", "bad"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("유효하지 않은 요청입니다."));

            when(chargerService.findChargers("", "", 1, 150))
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
                    4, 4.5, 2, List.of(), false, null
            );
            when(attractionService.findPopularNearbyAttractions(
                    new NearbySearchCondition(126.9780, 37.5665, 500.0, 20),
                    ""
            )).thenReturn(List.of(new PopularAttraction(attraction, 120.5, 42L)));

            mockMvc.perform(get("/api/attractions/popular-nearby"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.attractions[0].id").value(1))
                    .andExpect(jsonPath("$.data.attractions[0].title").value("경복궁"))
                    .andExpect(jsonPath("$.data.attractions[0].favoriteCount").value(4))
                    .andExpect(jsonPath("$.data.attractions[0].popularityCount").value(42))
                    .andExpect(jsonPath("$.data.attractions[0].distanceMeters").value(120.5));

            verify(attractionService).findPopularNearbyAttractions(
                    new NearbySearchCondition(126.9780, 37.5665, 500.0, 20),
                    ""
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
            when(attractionService.existsById(1L)).thenReturn(true);
            when(attractionService.findStats(1L, "ssafy")).thenReturn(new AttractionStats(
                    1L, 2, 4.5, 2, List.of(new AttractionTag(3L, "family")), true, 5
            ));
            when(attractionService.findAllTags()).thenReturn(List.of(new AttractionTag(3L, "family")));
            when(attractionService.replaceTags(1L, List.of(3L))).thenReturn(true);

            mockMvc.perform(put("/api/attractions/1/favorite").principal(jwtPrincipal("ssafy")))
                    .andExpect(status().isOk());
            verify(attractionService).addFavorite(1L, "ssafy");

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
                    .andExpect(jsonPath("$.data.stats.tags[0].name").value("family"));

            mockMvc.perform(put("/api/attractions/1/tags")
                            .principal(jwtPrincipal("ssafy"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"tagIds":[3]}
                                    """))
                    .andExpect(status().isOk());
            verify(attractionService).replaceTags(1L, List.of(3L));

            mockMvc.perform(get("/api/attraction-tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.tags[0].name").value("family"));
        }

        @DisplayName("헬스 체크는 DB 상태를 보고하고 전역 핸들러는 예상 밖 예외를 잡는다")
        @Test
        void healthReportsDatabaseStatusAndGlobalHandlerCatchesUnexpectedExceptions() throws Exception {
            when(dbHealthRepository.isConnected()).thenReturn(true);

            mockMvc.perform(get("/api/db/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.db").value("connected"));

            when(dbHealthRepository.isConnected()).thenReturn(false);
            mockMvc.perform(get("/api/db/health"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error.message")
                            .value("데이터베이스 연결이 끊어졌습니다."));

            mockMvc.perform(get("/test/fail"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"));
        }
    }

    @DisplayName("경로 엔드포인트는 잘못된 입력을 거부한다")
    @Test
    void routeEndpointsRejectInvalidInput() throws Exception {
        mockMvc.perform(get("/api/routes/optimizations").param("points", "37.5|bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("유효하지 않은 경로 좌표입니다."));

        mockMvc.perform(get("/api/routes/day-splits").param("points", "37.5|bad").param("days", "two"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("유효하지 않은 요청입니다."));
    }

    @DisplayName("컨트롤러 계약은 원시 Map 대신 DTO를 사용한다")
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

    @org.springframework.web.bind.annotation.RestController
    static class FailingController {
        @org.springframework.web.bind.annotation.GetMapping("/test/fail")
        String fail(Principal ignored) {
            throw new IllegalStateException("처리 중 오류가 발생했습니다.");
        }
    }
}

package com.ssafy.enjoytrip.core.api.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ssafy.enjoytrip.core.api.web.controller.NoteController;
import com.ssafy.enjoytrip.core.domain.Note;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NoteStatus;
import com.ssafy.enjoytrip.core.domain.NoteVisibility;
import com.ssafy.enjoytrip.core.domain.service.NoteService;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class NoteControllerTest {
    private NoteService noteService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        noteService = mock(NoteService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new NoteController(noteService)
                )
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @DisplayName("인증 사용자는 쪽지 추천 목록을 조회한다")
    @Test
    void authenticatedUserGetsNoteRecommendations() throws Exception {
        when(noteService.findRecommendations(11L, 10)).thenReturn(List.of(
                note(1L, "망원 산책 메모"),
                note(2L, "한강 일기")
        ));

        mockMvc.perform(get("/api/notes/recommendations")
                        .principal(jwtPrincipal(11L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notes[0].id").value(1))
                .andExpect(jsonPath("$.data.notes[0].title").value("망원 산책 메모"))
                .andExpect(jsonPath("$.data.notes[1].id").value(2))
                .andExpect(jsonPath("$.data.notes[1].title").value("한강 일기"));

        verify(noteService).findRecommendations(11L, 10);
    }

    @DisplayName("limit 파라미터를 지정하면 해당 limit으로 추천을 조회한다")
    @Test
    void noteRecommendationsRespectsLimitParameter() throws Exception {
        when(noteService.findRecommendations(11L, 5)).thenReturn(List.of(
                note(1L, "망원 산책 메모")
        ));

        mockMvc.perform(get("/api/notes/recommendations")
                        .param("limit", "5")
                        .principal(jwtPrincipal(11L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(noteService).findRecommendations(11L, 5);
    }

    @DisplayName("미인증 사용자가 쪽지 추천을 조회하면 401을 반환한다")
    @Test
    void unauthenticatedUserGetsUnauthorizedForNoteRecommendations() throws Exception {
        mockMvc.perform(get("/api/notes/recommendations"))
                .andExpect(status().isUnauthorized());

        verify(noteService, never()).findRecommendations(null, 10);
    }

    private static Note note(Long id, String title) {
        return new Note(
                id,
                11L,
                title,
                "내용",
                NoteCategory.UNCATEGORIZED,
                NoteVisibility.PUBLIC,
                37.5665,
                126.9780,
                "망원동",
                null,
                null,
                null,
                NoteStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }

    private static Principal jwtPrincipal(long memberId) {
        return () -> String.valueOf(memberId);
    }
}

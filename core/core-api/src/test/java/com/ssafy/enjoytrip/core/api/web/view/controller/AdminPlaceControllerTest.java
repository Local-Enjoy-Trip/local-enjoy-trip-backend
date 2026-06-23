package com.ssafy.enjoytrip.core.api.web.view.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ssafy.enjoytrip.core.domain.service.AttractionAdminService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminPlaceControllerTest {
    private AttractionAdminService attractionAdminService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        attractionAdminService = mock(AttractionAdminService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminPlaceController(attractionAdminService))
                .build();
    }

    @DisplayName("관리자 장소 숨김 AJAX 요청은 전체 페이지 리다이렉트 대신 행 갱신용 JSON을 반환한다")
    @Test
    void hideAjaxReturnsRowActionJson() throws Exception {
        when(attractionAdminService.findPlacesPage(true, 1, 20))
                .thenReturn(new AttractionAdminService.AdminPlacePage(List.of(), 1, 20, 25, 2));

        mockMvc.perform(post("/admin/places/10/hide")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("includeHidden", "true")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("HIDDEN"))
                .andExpect(jsonPath("$.visible").value(true))
                .andExpect(jsonPath("$.nextActionUrl").value("/admin/places/10/restore"))
                .andExpect(jsonPath("$.nextActionLabel").value("숨김 해제"));

        verify(attractionAdminService).hidePlace(10L);
    }

    @DisplayName("숨김 제외 목록에서 장소를 숨기면 AJAX 응답은 현재 행 제거를 지시한다")
    @Test
    void hideAjaxReturnsInvisibleWhenHiddenPlacesAreExcluded() throws Exception {
        when(attractionAdminService.findPlacesPage(false, 2, 20))
                .thenReturn(new AttractionAdminService.AdminPlacePage(List.of(), 2, 20, 39, 2));

        mockMvc.perform(post("/admin/places/10/hide")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("includeHidden", "false")
                        .param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HIDDEN"))
                .andExpect(jsonPath("$.visible").value(false))
                .andExpect(jsonPath("$.totalCount").value(39));

        verify(attractionAdminService).hidePlace(10L);
    }

    @DisplayName("관리자 장소 템플릿은 ACTIVE와 HIDDEN 상태별로 하나의 행 액션만 렌더링한다")
    @Test
    void placeTemplateRendersOneActionForEachStatus() throws Exception {
        String template = Files.readString(Path.of(
                "src/main/resources/templates/admin/places.html"
        ));

        assertThat(template)
                .contains("th:if=\"${place.status() == 'ACTIVE'}\"")
                .contains("th:if=\"${place.status() == 'HIDDEN'}\"")
                .contains("data-place-action-form");
    }
}

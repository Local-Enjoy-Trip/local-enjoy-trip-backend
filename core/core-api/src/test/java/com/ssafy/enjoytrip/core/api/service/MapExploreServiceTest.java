package com.ssafy.enjoytrip.core.domain.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.domain.query.NearbySearchCondition;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("service")
class MapExploreServiceTest {
    @DisplayName("MapExploreService는 SAVED_PLACE 필터에서 저장 장소 조건을 조회 경로로 전달한다")
    @Test
    void savedPlaceFilterDelegatesSavedOnlyPlaceLookup() {
        MemberService memberService = mock(MemberService.class);
        AttractionService attractionService = mock(AttractionService.class);
        NoteService noteService = mock(NoteService.class);
        MapExploreService service = new MapExploreService(memberService, attractionService, noteService);
        when(memberService.findByUserId("viewer")).thenReturn(new Member(
                "viewer",
                "Viewer",
                "viewer@example.com",
                "secret",
                ""
        ));
        when(attractionService.findNearbyCandidates(
                new NearbySearchCondition(126.9780, 37.5665, 500.0, 50),
                "viewer",
                true
        )).thenReturn(List.of());

        service.explore(
                "viewer",
                126.9780,
                37.5665,
                500.0,
                50,
                MapExploreFilter.SAVED_PLACE,
                null
        );

        verify(attractionService).findNearbyCandidates(
                new NearbySearchCondition(126.9780, 37.5665, 500.0, 50),
                "viewer",
                true
        );
    }
}

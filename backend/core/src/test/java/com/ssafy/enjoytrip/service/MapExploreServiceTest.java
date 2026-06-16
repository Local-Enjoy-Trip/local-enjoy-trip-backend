package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.support.error.ErrorType.MEMBER_REPRESENTATIVE_LOCATION_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.dto.query.AttractionSearchCondition;
import com.ssafy.enjoytrip.domain.AttractionStats;
import com.ssafy.enjoytrip.domain.AttractionTag;
import com.ssafy.enjoytrip.dto.command.CreateNoteCommand;
import com.ssafy.enjoytrip.dto.query.MapExploreCommand;
import com.ssafy.enjoytrip.domain.MapExploreFilter;
import com.ssafy.enjoytrip.domain.MapExploreResult;
import com.ssafy.enjoytrip.dto.query.MapNotesCondition;
import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.domain.NearbyAttractionCandidate;
import com.ssafy.enjoytrip.dto.query.NearbyNotesCondition;
import com.ssafy.enjoytrip.dto.query.NearbySearchCondition;
import com.ssafy.enjoytrip.domain.Note;
import com.ssafy.enjoytrip.domain.NoteMapPin;
import com.ssafy.enjoytrip.dto.command.UpdateNoteCommand;
import com.ssafy.enjoytrip.repository.AttractionRepository;
import com.ssafy.enjoytrip.repository.MemberRepository;
import com.ssafy.enjoytrip.repository.NoteRepository;
import com.ssafy.enjoytrip.support.error.CoreException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MapExploreServiceTest {

    @DisplayName("지도 탐색은 요청 좌표가 없으면 인증 사용자의 대표 동네 좌표를 사용한다")
    @Test
    void usesRepresentativeLocationWhenCoordinatesAreMissing() {
        FakeAttractionRepository attractionRepository = new FakeAttractionRepository();
        FakeNoteRepository noteRepository = new FakeNoteRepository();
        MapExploreService service = new MapExploreService(
                new FakeMemberRepository(memberWithRepresentativeLocation()),
                attractionRepository,
                noteRepository
        );

        MapExploreResult result = service.explore(command(null, null, MapExploreFilter.ALL));

        assertTrue(result.center().fromRepresentativeLocation());
        assertEquals(126.9780, result.center().longitude());
        assertEquals(37.5665, result.center().latitude());
        assertEquals("서울 중구", result.center().regionName());
        assertEquals(126.9780, attractionRepository.lastCondition.longitude());
        assertEquals(37.5665, attractionRepository.lastCondition.latitude());
        assertEquals(126.9780, noteRepository.lastCondition.longitude());
        assertEquals(37.5665, noteRepository.lastCondition.latitude());
    }

    @DisplayName("지도 탐색은 요청 좌표가 있으면 대표 동네보다 요청 좌표를 우선한다")
    @Test
    void explicitCoordinatesOverrideRepresentativeLocation() {
        FakeAttractionRepository attractionRepository = new FakeAttractionRepository();
        FakeNoteRepository noteRepository = new FakeNoteRepository();
        MapExploreService service = new MapExploreService(
                new FakeMemberRepository(memberWithRepresentativeLocation()),
                attractionRepository,
                noteRepository
        );

        MapExploreResult result = service.explore(command(127.1, 37.7, MapExploreFilter.ALL));

        assertEquals(127.1, result.center().longitude());
        assertEquals(37.7, result.center().latitude());
        assertNull(result.center().regionName());
        assertFalse(result.center().fromRepresentativeLocation());
        assertEquals(127.1, attractionRepository.lastCondition.longitude());
        assertEquals(37.7, noteRepository.lastCondition.latitude());
    }

    @DisplayName("대표 동네 좌표가 없으면 전용 비즈니스 오류를 반환한다")
    @Test
    void missingCoordinatesAndRepresentativeLocationThrowsBusinessError() {
        MapExploreService service = new MapExploreService(
                new FakeMemberRepository(memberWithoutRepresentativeLocation()),
                new FakeAttractionRepository(),
                new FakeNoteRepository()
        );

        CoreException exception = assertThrows(
                CoreException.class,
                () -> service.explore(command(null, null, MapExploreFilter.ALL))
        );

        assertEquals(MEMBER_REPRESENTATIVE_LOCATION_REQUIRED, exception.errorType());
    }

    @DisplayName("지도 탐색 FRIEND 필터는 장소를 제외하고 친구 쪽지 조건만 저장소에 위임한다")
    @Test
    void friendFilterDelegatesFriendsOnlyNoteQuery() {
        FakeAttractionRepository attractionRepository = new FakeAttractionRepository();
        FakeNoteRepository noteRepository = new FakeNoteRepository();
        MapExploreService service = new MapExploreService(
                new FakeMemberRepository(memberWithRepresentativeLocation()),
                attractionRepository,
                noteRepository
        );

        service.explore(new MapExploreCommand(
                "ssafy",
                null,
                null,
                800.0,
                20,
                MapExploreFilter.FRIEND,
                null
        ));

        assertNull(attractionRepository.lastCondition);
        assertTrue(noteRepository.lastCondition.friendOnly());
        assertEquals(800.0, noteRepository.lastCondition.radiusMeters());
        assertEquals(20, noteRepository.lastCondition.limit());
    }

    private static MapExploreCommand command(Double longitude, Double latitude, MapExploreFilter filter) {
        return new MapExploreCommand("ssafy", longitude, latitude, 500.0, 50, filter, null);
    }

    private static Member memberWithRepresentativeLocation() {
        return new Member(
                "ssafy",
                "싸피",
                "동네핀",
                "ssafy@example.com",
                "pw",
                "https://example.com/profile.png",
                37.5665,
                126.9780,
                "서울 중구",
                "created"
        );
    }

    private static Member memberWithoutRepresentativeLocation() {
        return new Member("ssafy", "싸피", "동네핀", "ssafy@example.com", "pw", null, null, null, null, "created");
    }

    private static class FakeMemberRepository implements MemberRepository {
        private final Member member;

        FakeMemberRepository(Member member) {
            this.member = member;
        }

        @Override
        public List<Member> findAll() {
            return List.of(member);
        }

        @Override
        public Member findByUserId(String userId) {
            return member;
        }

        @Override
        public Member findByEmail(String email) {
            return null;
        }

        @Override
        public String findPassword(String userId, String email) {
            return null;
        }

        @Override
        public boolean existsByUserId(String userId) {
            return member != null && member.userId().equals(userId);
        }

        @Override
        public boolean existsByEmail(String email) {
            return false;
        }

        @Override
        public void insert(Member member) {
        }

        @Override
        public boolean update(Member member) {
            return false;
        }

        @Override
        public boolean delete(String userId) {
            return false;
        }

        @Override
        public void insertAuthLog(String userId, String eventType) {
        }
    }

    private static class FakeAttractionRepository implements AttractionRepository {
        private NearbySearchCondition lastCondition;

        @Override
        public List<Attraction> search(AttractionSearchCondition condition) {
            return List.of();
        }

        @Override
        public List<NearbyAttractionCandidate> findNearbyCandidates(NearbySearchCondition condition,
                                                                    String userId) {
            lastCondition = condition;
            return List.of();
        }

        @Override
        public boolean existsById(Long attractionId) {
            return false;
        }

        @Override
        public AttractionStats findStats(Long attractionId, String userId) {
            return null;
        }

        @Override
        public void addFavorite(Long attractionId, String userId) {
        }

        @Override
        public boolean removeFavorite(Long attractionId, String userId) {
            return false;
        }

        @Override
        public void upsertRating(Long attractionId, String userId, int rating) {
        }

        @Override
        public boolean removeRating(Long attractionId, String userId) {
            return false;
        }

        @Override
        public List<AttractionTag> findAllTags() {
            return List.of();
        }

        @Override
        public AttractionTag insertTag(String name) {
            return null;
        }

        @Override
        public boolean updateTag(Long tagId, String name) {
            return false;
        }

        @Override
        public boolean deleteTag(Long tagId) {
            return false;
        }

        @Override
        public boolean replaceTags(Long attractionId, List<Long> tagIds) {
            return false;
        }
    }

    private static class FakeNoteRepository implements NoteRepository {
        private MapNotesCondition lastCondition;

        @Override
        public Note save(CreateNoteCommand command) {
            return null;
        }

        @Override
        public Optional<Note> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public Optional<Note> updateOwned(UpdateNoteCommand command) {
            return Optional.empty();
        }

        @Override
        public boolean softDeleteOwned(Long id, String authorUserId) {
            return false;
        }

        @Override
        public List<Note> findNearbyAccessible(NearbyNotesCondition condition, String viewerUserId) {
            return List.of();
        }

        @Override
        public List<NoteMapPin> findMapNotes(MapNotesCondition condition) {
            lastCondition = condition;
            return List.of();
        }
    }
}

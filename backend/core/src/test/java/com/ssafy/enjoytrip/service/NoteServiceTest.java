package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.support.error.ErrorType.AUTHENTICATION_REQUIRED;
import static com.ssafy.enjoytrip.support.error.ErrorType.NOTE_ACCESS_DENIED;
import static com.ssafy.enjoytrip.support.error.ErrorType.NOTE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ssafy.enjoytrip.domain.CreateNoteCommand;
import com.ssafy.enjoytrip.domain.NearbyNotesCondition;
import com.ssafy.enjoytrip.domain.Note;
import com.ssafy.enjoytrip.domain.NoteCategory;
import com.ssafy.enjoytrip.domain.NoteStatus;
import com.ssafy.enjoytrip.domain.NoteVisibility;
import com.ssafy.enjoytrip.domain.UpdateNoteCommand;
import com.ssafy.enjoytrip.repository.NoteRepository;
import com.ssafy.enjoytrip.support.error.CoreException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoteServiceTest {

    @DisplayName("쪽지 생성은 인증 작성자를 요구하고 저장소에 위임한다")
    @Test
    void createRequiresAuthenticatedAuthorAndDelegatesToRepository() {
        Note note = activeNote(1L, "ssafy");
        FakeNoteRepository repository = new FakeNoteRepository();
        repository.savedNote = note;
        NoteService service = new NoteService(repository);
        CreateNoteCommand command = createCommand("ssafy");

        Note result = service.createNote(command);

        assertEquals(note, result);
        assertEquals(command, repository.lastCreateCommand);
    }

    @DisplayName("쪽지 생성은 인증 작성자가 없으면 거부한다")
    @Test
    void createRejectsAnonymousAuthor() {
        NoteService service = new NoteService(new FakeNoteRepository());
        CreateNoteCommand command = createCommand(" ");

        CoreException exception = assertThrows(CoreException.class, () -> service.createNote(command));

        assertEquals(AUTHENTICATION_REQUIRED, exception.errorType());
    }

    @DisplayName("쪽지 수정은 active 작성자 본인일 때만 guarded update를 수행한다")
    @Test
    void updateRequiresActiveOwnedNoteBeforeGuardedMutation() {
        Note existing = activeNote(1L, "ssafy");
        Note updated = activeNote(1L, "ssafy", "수정 제목");
        FakeNoteRepository repository = new FakeNoteRepository();
        repository.findByIdResult = Optional.of(existing);
        repository.updateOwnedResult = Optional.of(updated);
        NoteService service = new NoteService(repository);
        UpdateNoteCommand command = updateCommand(1L, "ssafy");

        Note result = service.updateNote(command);

        assertEquals(updated, result);
        assertEquals(command, repository.lastUpdateCommand);
    }

    @DisplayName("쪽지 수정은 없는 쪽지와 비활성 쪽지를 모두 not found로 숨긴다")
    @Test
    void updateHidesMissingAndInactiveNotesAsNotFound() {
        FakeNoteRepository missingRepository = new FakeNoteRepository();
        NoteService missingService = new NoteService(missingRepository);

        CoreException missing = assertThrows(CoreException.class, () -> missingService.updateNote(updateCommand(1L, "ssafy")));
        assertEquals(NOTE_NOT_FOUND, missing.errorType());

        FakeNoteRepository deletedRepository = new FakeNoteRepository();
        deletedRepository.findByIdResult = Optional.of(note(1L, "ssafy", NoteStatus.DELETED, "삭제됨"));
        NoteService deletedService = new NoteService(deletedRepository);

        CoreException deleted = assertThrows(CoreException.class, () -> deletedService.updateNote(updateCommand(1L, "ssafy")));
        assertEquals(NOTE_NOT_FOUND, deleted.errorType());
    }

    @DisplayName("쪽지 수정은 active 쪽지의 작성자가 다르면 forbidden으로 거부한다")
    @Test
    void updateRejectsNonOwnerAsForbidden() {
        FakeNoteRepository repository = new FakeNoteRepository();
        repository.findByIdResult = Optional.of(activeNote(1L, "author"));
        NoteService service = new NoteService(repository);

        CoreException exception = assertThrows(CoreException.class, () -> service.updateNote(updateCommand(1L, "other")));

        assertEquals(NOTE_ACCESS_DENIED, exception.errorType());
    }

    @DisplayName("쪽지 삭제는 active 작성자 본인일 때 soft delete를 수행한다")
    @Test
    void deleteSoftDeletesOwnedActiveNote() {
        FakeNoteRepository repository = new FakeNoteRepository();
        repository.findByIdResult = Optional.of(activeNote(1L, "ssafy"));
        repository.softDeleteResult = true;
        NoteService service = new NoteService(repository);

        service.deleteNote(1L, "ssafy");

        assertEquals(1L, repository.lastDeletedId);
        assertEquals("ssafy", repository.lastDeletedAuthorUserId);
    }

    @DisplayName("주변 쪽지 조회는 viewer를 빈 문자열로 정규화해 저장소에 위임한다")
    @Test
    void nearbyNotesNormalizeBlankViewerAndDelegateToRepository() {
        NearbyNotesCondition condition = new NearbyNotesCondition(126.9780, 37.5665, 500, 20);
        Note note = activeNote(1L, "ssafy");
        FakeNoteRepository repository = new FakeNoteRepository();
        repository.nearbyNotes = List.of(note);
        NoteService service = new NoteService(repository);

        List<Note> result = service.findNearbyNotes(condition, null);

        assertEquals(List.of(note), result);
        assertEquals(condition, repository.lastNearbyCondition);
        assertEquals("", repository.lastViewerUserId);
    }

    private static CreateNoteCommand createCommand(String authorUserId) {
        return new CreateNoteCommand(
                authorUserId,
                "서울 산책",
                "날씨 좋음",
                NoteCategory.TIP,
                NoteVisibility.PUBLIC,
                37.5665,
                126.9780,
                "서울"
        );
    }

    private static UpdateNoteCommand updateCommand(Long id, String authorUserId) {
        return new UpdateNoteCommand(
                id,
                authorUserId,
                "수정 제목",
                "수정 내용",
                NoteCategory.TIP,
                NoteVisibility.PUBLIC,
                37.5665,
                126.9780,
                "서울"
        );
    }

    private static Note activeNote(Long id, String authorUserId) {
        return activeNote(id, authorUserId, "서울 산책");
    }

    private static Note activeNote(Long id, String authorUserId, String title) {
        return note(id, authorUserId, NoteStatus.ACTIVE, title);
    }

    private static Note note(Long id, String authorUserId, NoteStatus status, String title) {
        return new Note(
                id,
                authorUserId,
                title,
                "content",
                NoteCategory.TIP,
                NoteVisibility.PUBLIC,
                37.5665,
                126.9780,
                "서울",
                status,
                LocalDateTime.of(2026, 6, 10, 10, 0),
                null,
                null
        );
    }

    private static class FakeNoteRepository implements NoteRepository {
        private CreateNoteCommand lastCreateCommand;
        private UpdateNoteCommand lastUpdateCommand;
        private Long lastDeletedId;
        private String lastDeletedAuthorUserId;
        private NearbyNotesCondition lastNearbyCondition;
        private String lastViewerUserId;
        private Note savedNote;
        private Optional<Note> findByIdResult = Optional.empty();
        private Optional<Note> updateOwnedResult = Optional.empty();
        private boolean softDeleteResult;
        private List<Note> nearbyNotes = List.of();

        @Override
        public Note save(CreateNoteCommand command) {
            lastCreateCommand = command;
            return savedNote;
        }

        @Override
        public Optional<Note> findById(Long id) {
            return findByIdResult;
        }

        @Override
        public Optional<Note> updateOwned(UpdateNoteCommand command) {
            lastUpdateCommand = command;
            return updateOwnedResult;
        }

        @Override
        public boolean softDeleteOwned(Long id, String authorUserId) {
            lastDeletedId = id;
            lastDeletedAuthorUserId = authorUserId;
            return softDeleteResult;
        }

        @Override
        public List<Note> findNearbyAccessible(NearbyNotesCondition condition, String viewerUserId) {
            lastNearbyCondition = condition;
            lastViewerUserId = viewerUserId;
            return nearbyNotes;
        }
    }
}

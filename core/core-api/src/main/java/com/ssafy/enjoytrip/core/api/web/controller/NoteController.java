package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId.Unauthenticated.NULL;
import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.Note;
import com.ssafy.enjoytrip.core.domain.service.NoteService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.NoteApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.NearbySectionRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.NoteCreateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.NoteUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.NoteUpdateTagsRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.SavedNotesRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.WrittenNotesRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.NoteResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.NotesResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController implements NoteApi {
    private final NoteService service;

    @PostMapping
    @Override
    public ApiResponse<NoteResponse> create(@RequestBody NoteCreateRequest request,
                                            @AuthenticatedMemberId Long memberId) {
        Note note = service.createNote(request.toNote(memberId));

        return success(new NoteResponse(note));
    }

    @PutMapping("/{id}")
    @Override
    public ApiResponse<NoteResponse> update(@PathVariable Long id,
                                            @RequestBody NoteUpdateRequest request,
                                            @AuthenticatedMemberId Long memberId) {
        Note note = service.updateNote(request.toNote(id, memberId));

        return success(new NoteResponse(note));
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticatedMemberId Long memberId) {
        service.deleteNote(id, memberId);

        return success();
    }

    @PutMapping("/{id}/save")
    @Override
    public ApiResponse<Void> save(@PathVariable Long id, @AuthenticatedMemberId Long memberId) {
        service.addSave(id, memberId);

        return success();
    }

    @DeleteMapping("/{id}/save")
    @Override
    public ApiResponse<Void> unsave(@PathVariable Long id, @AuthenticatedMemberId Long memberId) {
        service.removeSave(id, memberId);

        return success();
    }

    @PutMapping("/{id}/tags")
    @Override
    public ApiResponse<Void> updateTags(
            @PathVariable Long id,
            @RequestBody NoteUpdateTagsRequest request,
            @AuthenticatedMemberId Long memberId
    ) {
        service.updateNoteTags(id, memberId, request.toTags());
        return success();
    }

    @GetMapping("/saved")
    @Override
    public ApiResponse<NotesResponse> saved(
            @ModelAttribute SavedNotesRequest request,
            @AuthenticatedMemberId Long memberId
    ) {
        List<Note> notes = service.findSavedNotes(memberId, request.normalizedLimit());

        return success(NotesResponse.from(notes));
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<NotesResponse> me(
            @ModelAttribute WrittenNotesRequest request,
            @AuthenticatedMemberId Long memberId
    ) {
        List<Note> notes = service.findWrittenNotes(memberId, request.normalizedLimit());

        return success(NotesResponse.from(notes));
    }

    @GetMapping("/nearby")
    @Override
    public ApiResponse<NotesResponse> nearby(
            @ModelAttribute NearbySectionRequest request,
            @AuthenticatedMemberId(unauthenticated = NULL) Long memberId
    ) {
        List<Note> notes = service.findNearbyNotes(
                request.toCondition(),
                memberId
        );

        return success(NotesResponse.from(notes));
    }

}

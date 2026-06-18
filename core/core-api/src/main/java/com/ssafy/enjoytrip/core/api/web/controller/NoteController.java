package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.api.security.AuthenticatedUserId.Unauthenticated.NULL;
import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.Note;
import com.ssafy.enjoytrip.core.domain.service.NoteService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.NoteApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.NearbySectionRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.NoteCreateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.NoteUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.NoteResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.NotesResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.ssafy.enjoytrip.core.api.security.AuthenticatedUserId;
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
    public ApiResponse<NoteResponse> create(@Valid @RequestBody NoteCreateRequest request,
                                            @AuthenticatedUserId String authenticatedUserId) {
        Note note = service.createNote(request.toNote(authenticatedUserId));

        return success(new NoteResponse(note));
    }

    @PutMapping("/{id}")
    @Override
    public ApiResponse<NoteResponse> update(@PathVariable Long id,
                                            @Valid @RequestBody NoteUpdateRequest request,
                                            @AuthenticatedUserId String authenticatedUserId) {
        Note note = service.updateNote(request.toNote(id, authenticatedUserId));

        return success(new NoteResponse(note));
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticatedUserId String authenticatedUserId) {
        service.deleteNote(id, authenticatedUserId);

        return success();
    }

    @GetMapping("/nearby")
    @Override
    public ApiResponse<NotesResponse> nearby(
            @Valid @ModelAttribute NearbySectionRequest request,
            @AuthenticatedUserId(unauthenticated = NULL) String authenticatedUserId
    ) {
        List<Note> notes = service.findNearbyNotes(
                request.toNotesCondition(),
                authenticatedUserId
        );

        return success(NotesResponse.from(notes));
    }

}

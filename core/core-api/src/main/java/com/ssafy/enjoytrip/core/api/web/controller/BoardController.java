package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.POST_NOT_FOUND;
import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.BoardPost;
import com.ssafy.enjoytrip.core.domain.service.BoardService;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.BoardApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.BoardCreateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.BoardUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.BoardsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController implements BoardApi {
    private final BoardService service;

    @GetMapping
    @Override
    public ApiResponse<BoardsResponse> findAll() {
        return success(new BoardsResponse(service.findAllPosts()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<Void> create(@Valid @RequestBody BoardCreateRequest request) {
        service.insertPost(new BoardPost(
                request.normalizedId(),
                request.normalizedTitle(),
                request.normalizedContent(),
                request.normalizedAuthor(),
                "",
                ""
        ));

        return success();
    }

    @PutMapping("/{id}")
    @Override
    public ApiResponse<Void> update(@PathVariable String id, @Valid @RequestBody BoardUpdateRequest request) {
        if (service.updatePost(new BoardPost(
                id.strip(),
                request.normalizedTitle(),
                request.normalizedContent(),
                "",
                "",
                ""
        ))) {
            return success();
        }

        throw new CoreException(POST_NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable String id) {
        if (service.deletePost(id.strip())) {
            return success();
        }

        throw new CoreException(POST_NOT_FOUND);
    }
}

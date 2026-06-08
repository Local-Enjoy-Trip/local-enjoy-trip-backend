package com.ssafy.enjoytrip.web.controller;

import com.ssafy.enjoytrip.web.api.*;

import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_ACTION;
import static com.ssafy.enjoytrip.support.error.ErrorType.MISSING_ID;
import static com.ssafy.enjoytrip.support.error.ErrorType.MISSING_REQUIRED_FIELDS;
import static com.ssafy.enjoytrip.support.error.ErrorType.POST_NOT_FOUND;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.BoardPost;
import com.ssafy.enjoytrip.service.BoardService;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.error.ErrorType;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.BoardRequest;
import com.ssafy.enjoytrip.web.dto.response.BoardsResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    @Override
    public ApiResponse<Void> legacyPost(@ModelAttribute BoardRequest request) {
        return switch (trim(request.action())) {
            case "create" -> create(request);
            case "update" -> update(trim(request.id()), request);
            case "delete" -> delete(trim(request.id()));
            default -> fail(INVALID_ACTION);
        };
    }

    @PostMapping("/posts")
    @Override
    public ApiResponse<Void> create(@ModelAttribute BoardRequest request) {
        String id = trim(request.id());
        String title = trim(request.title());
        String content = trim(request.content());
        String author = trim(request.author());
        if (id.isEmpty() || title.isEmpty() || content.isEmpty() || author.isEmpty()) {
            return fail(MISSING_REQUIRED_FIELDS);
        }
        service.insertPost(new BoardPost(id, title, content, author, "", ""));
        return success();
    }

    @PutMapping("/{id}")
    @Override
    public ApiResponse<Void> update(@PathVariable String id, @ModelAttribute BoardRequest request) {
        String title = trim(request.title());
        String content = trim(request.content());
        if (trim(id).isEmpty() || title.isEmpty() || content.isEmpty()) {
            return fail(MISSING_REQUIRED_FIELDS);
        }
        if (service.updatePost(new BoardPost(id, title, content, "", "", ""))) {
            return success();
        }
        return fail(POST_NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable String id) {
        if (trim(id).isEmpty()) {
            return fail(MISSING_ID);
        }
        if (service.deletePost(id)) {
            return success();
        }
        return fail(POST_NOT_FOUND);
    }

    private static <T> T fail(ErrorType error) {
        throw new CoreException(error);
    }

    private static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

}

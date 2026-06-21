package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.AttractionTag;
import com.ssafy.enjoytrip.core.domain.service.AttractionService;
import com.ssafy.enjoytrip.core.support.error.exception.ClientInputException;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.AttractionTagApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.TagRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.AttractionTagsResponse;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/attraction-tags")
@RequiredArgsConstructor
public class AttractionTagController implements AttractionTagApi {
    private final AttractionService service;

    @GetMapping
    @Override
    public ApiResponse<AttractionTagsResponse> tags() {
        return success(new AttractionTagsResponse(service.findAllTags()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<AttractionTagsResponse> create(@Valid @RequestBody TagRequest request) {
        String name = request.normalizedName();
        AttractionTag tag = service.createTagOrThrow(name);
        return success(new AttractionTagsResponse(List.of(tag)));
    }

    @PutMapping("/{id}")
    @Override
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        requireId(id);
        String name = request.normalizedName();
        service.updateTagOrThrow(id, name);
        return success();
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireId(id);
        service.deleteTagOrThrow(id);
        return success();
    }

    private static void requireId(Long id) {
        if (id == null || id <= 0) {
            throw new ClientInputException("유효하지 않은 id입니다.");
        }
    }
}

package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_ID;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.TAG_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.TAG_NOT_FOUND;
import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.AttractionTag;
import com.ssafy.enjoytrip.core.domain.service.AttractionService;
import com.ssafy.enjoytrip.core.support.error.CoreException;
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
        if (tagNameExists(null, name)) {
            throw new CoreException(TAG_ALREADY_EXISTS);
        }
        AttractionTag tag = service.insertTag(name);
        return success(new AttractionTagsResponse(List.of(tag)));
    }

    @PutMapping("/{id}")
    @Override
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        requireId(id);
        String name = request.normalizedName();
        if (tagNameExists(id, name)) {
            throw new CoreException(TAG_ALREADY_EXISTS);
        }
        if (!service.updateTag(id, name)) {
            throw new CoreException(TAG_NOT_FOUND);
        }
        return success();
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireId(id);
        if (!service.deleteTag(id)) {
            throw new CoreException(TAG_NOT_FOUND);
        }
        return success();
    }

    private boolean tagNameExists(Long currentId, String name) {
        return service.findAllTags().stream()
                .anyMatch(tag -> tag.name().equals(name) && !tag.id().equals(currentId));
    }

    private static void requireId(Long id) {
        if (id == null || id <= 0) {
            throw new CoreException(INVALID_ID);
        }
    }
}

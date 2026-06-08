package com.ssafy.enjoytrip.web.controller;

import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_ID;
import static com.ssafy.enjoytrip.support.error.ErrorType.MISSING_REQUIRED_FIELDS;
import static com.ssafy.enjoytrip.support.error.ErrorType.TAG_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.support.error.ErrorType.TAG_NOT_FOUND;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.AttractionTag;
import com.ssafy.enjoytrip.service.AttractionService;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.error.ErrorType;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.api.AttractionTagApi;
import com.ssafy.enjoytrip.web.dto.request.TagRequest;
import com.ssafy.enjoytrip.web.dto.response.AttractionTagsResponse;
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
    @Override
    public ApiResponse<AttractionTagsResponse> create(@ModelAttribute TagRequest request) {
        String name = requireName(request.name());
        if (tagNameExists(null, name)) {
            return fail(TAG_ALREADY_EXISTS);
        }
        AttractionTag tag = service.insertTag(name);
        return success(new AttractionTagsResponse(List.of(tag)));
    }

    @PutMapping("/{id}")
    @Override
    public ApiResponse<Void> update(@PathVariable Long id, @ModelAttribute TagRequest request) {
        requireId(id);
        String name = requireName(request.name());
        if (tagNameExists(id, name)) {
            return fail(TAG_ALREADY_EXISTS);
        }
        if (!service.updateTag(id, name)) {
            return fail(TAG_NOT_FOUND);
        }
        return success();
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireId(id);
        if (!service.deleteTag(id)) {
            return fail(TAG_NOT_FOUND);
        }
        return success();
    }

    private boolean tagNameExists(Long currentId, String name) {
        return service.findAllTags().stream()
                .anyMatch(tag -> tag.name().equals(name) && !tag.id().equals(currentId));
    }

    private static String requireName(String raw) {
        String name = trim(raw);
        if (name.isEmpty()) {
            fail(MISSING_REQUIRED_FIELDS);
        }
        return name;
    }

    private static void requireId(Long id) {
        if (id == null || id <= 0) {
            fail(INVALID_ID);
        }
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

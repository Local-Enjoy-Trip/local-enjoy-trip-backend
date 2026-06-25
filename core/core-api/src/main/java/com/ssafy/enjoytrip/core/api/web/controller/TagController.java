package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.Tag;
import com.ssafy.enjoytrip.core.domain.service.TagService;
import com.ssafy.enjoytrip.core.support.error.exception.ClientInputException;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.TagApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.TagRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.TagsResponse;
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
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController implements TagApi {
    private final TagService tagService;

    @GetMapping
    @Override
    public ApiResponse<TagsResponse> tags() {
        return success(new TagsResponse(tagService.findAll()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<TagsResponse> create(@Valid @RequestBody TagRequest request) {
        String name = request.normalizedName();
        Tag tag = tagService.create(name);
        return success(new TagsResponse(List.of(tag)));
    }

    @PutMapping("/{id}")
    @Override
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        requireId(id);
        tagService.updateOrThrow(id, request.normalizedName());
        return success();
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireId(id);
        tagService.deleteOrThrow(id);
        return success();
    }

    private static void requireId(Long id) {
        if (id == null || id <= 0) {
            throw new ClientInputException("유효하지 않은 id입니다.");
        }
    }
}

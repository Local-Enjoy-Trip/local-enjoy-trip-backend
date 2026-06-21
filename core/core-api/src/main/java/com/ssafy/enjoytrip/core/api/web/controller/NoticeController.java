package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.Notice;
import com.ssafy.enjoytrip.core.domain.service.NoticeService;
import com.ssafy.enjoytrip.core.support.error.exception.ClientInputException;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.NoticeApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.NoticeCreateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.NoticeUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.NoticesResponse;
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
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController implements NoticeApi {
    private final NoticeService service;

    @GetMapping
    @Override
    public ApiResponse<NoticesResponse> findAll() {
        return success(new NoticesResponse(service.findAllNotices()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<Void> create(@Valid @RequestBody NoticeCreateRequest request) {
        service.insertNotice(new Notice(
                null,
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
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody NoticeUpdateRequest request) {
        requireId(id);
        service.updateNoticeOrThrow(new Notice(
                id,
                request.normalizedTitle(),
                request.normalizedContent(),
                "",
                "",
                ""
        ));

        return success();
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireId(id);
        service.deleteNoticeOrThrow(id);
        return success();
    }

    private static void requireId(Long id) {
        if (id == null || id <= 0) {
            throw new ClientInputException("유효하지 않은 id입니다.");
        }
    }
}

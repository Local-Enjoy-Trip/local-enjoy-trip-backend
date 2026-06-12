package com.ssafy.enjoytrip.web.controller;

import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_ID;
import static com.ssafy.enjoytrip.support.error.ErrorType.NOTICE_NOT_FOUND;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.Notice;
import com.ssafy.enjoytrip.service.NoticeService;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.api.NoticeApi;
import com.ssafy.enjoytrip.web.dto.request.NoticeCreateRequest;
import com.ssafy.enjoytrip.web.dto.request.NoticeUpdateRequest;
import com.ssafy.enjoytrip.web.dto.response.NoticesResponse;
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

    @PostMapping("/items")
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
        if (service.updateNotice(new Notice(
                id,
                request.normalizedTitle(),
                request.normalizedContent(),
                "",
                "",
                ""
        ))) {
            return success();
        }

        throw new CoreException(NOTICE_NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireId(id);
        if (service.deleteNotice(id)) {
            return success();
        }

        throw new CoreException(NOTICE_NOT_FOUND);
    }

    private static void requireId(Long id) {
        if (id == null || id <= 0) {
            throw new CoreException(INVALID_ID);
        }
    }
}

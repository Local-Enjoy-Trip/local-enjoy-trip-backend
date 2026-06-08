package com.ssafy.enjoytrip.web.controller;

import com.ssafy.enjoytrip.web.api.*;

import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_ACTION;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_ID;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_REQUEST;
import static com.ssafy.enjoytrip.support.error.ErrorType.MISSING_REQUIRED_FIELDS;
import static com.ssafy.enjoytrip.support.error.ErrorType.NOTICE_NOT_FOUND;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.Notice;
import com.ssafy.enjoytrip.service.NoticeService;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.error.ErrorType;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.NoticeRequest;
import com.ssafy.enjoytrip.web.dto.response.NoticesResponse;
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
    @Override
    public ApiResponse<Void> legacyPost(@ModelAttribute NoticeRequest request) {
        return switch (trim(request.action())) {
            case "create" -> create(request);
            case "update" -> update(parseLong(request.id()), request);
            case "delete" -> delete(parseLong(request.id()));
            default -> fail(INVALID_ACTION);
        };
    }

    @PostMapping("/items")
    @Override
    public ApiResponse<Void> create(@ModelAttribute NoticeRequest request) {
        String title = trim(request.title());
        String content = trim(request.content());
        String author = trim(request.author());
        if (title.isEmpty() || content.isEmpty() || author.isEmpty()) {
            return fail(MISSING_REQUIRED_FIELDS);
        }
        service.insertNotice(new Notice(null, title, content, author, "", ""));
        return success();
    }

    @PutMapping("/{id}")
    @Override
    public ApiResponse<Void> update(@PathVariable Long id, @ModelAttribute NoticeRequest request) {
        String title = trim(request.title());
        String content = trim(request.content());
        if (id == null || id <= 0 || title.isEmpty() || content.isEmpty()) {
            return fail(INVALID_REQUEST);
        }
        if (service.updateNotice(new Notice(id, title, content, "", "", ""))) {
            return success();
        }
        return fail(NOTICE_NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return fail(INVALID_ID);
        }
        if (service.deleteNotice(id)) {
            return success();
        }
        return fail(NOTICE_NOT_FOUND);
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

    private static Long parseLong(String raw) {
        String value = trim(raw);
        if (value.isEmpty()) {
            return -1L;
        }
        if (!isLong(value)) {
            return -1L;
        }
        return Long.parseLong(value);
    }

    private static boolean isLong(String value) {
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (i == 0 && current == '-') {
                continue;
            }
            if (!Character.isDigit(current)) {
                return false;
            }
        }
        return !value.equals("-");
    }

}

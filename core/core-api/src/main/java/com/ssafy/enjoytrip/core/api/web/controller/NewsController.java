package com.ssafy.enjoytrip.core.api.web.controller;

import com.ssafy.enjoytrip.core.api.web.api.*;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.api.web.dto.response.NewsItem;
import com.ssafy.enjoytrip.core.domain.service.NewsService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.NewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController implements NewsApi {
    private final NewsService service;

    @GetMapping
    @Override
    public ApiResponse<NewsResponse> findNews() {
        return success(new NewsResponse(service.findNews()));
    }

}

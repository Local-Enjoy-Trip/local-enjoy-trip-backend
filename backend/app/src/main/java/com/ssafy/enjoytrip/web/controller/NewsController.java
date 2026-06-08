package com.ssafy.enjoytrip.web.controller;

import com.ssafy.enjoytrip.web.api.*;

import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.NewsItem;
import com.ssafy.enjoytrip.service.NewsService;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.response.NewsResponse;
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

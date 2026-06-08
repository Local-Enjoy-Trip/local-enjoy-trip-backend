package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.NewsItem;
import com.ssafy.enjoytrip.repository.NewsRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository repository;

    public List<NewsItem> findNews() {
        return repository.findNews();
    }
}

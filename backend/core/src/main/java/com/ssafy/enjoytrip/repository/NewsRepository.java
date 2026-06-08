package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.NewsItem;

import java.util.List;

public interface NewsRepository {
    List<NewsItem> findNews();
}

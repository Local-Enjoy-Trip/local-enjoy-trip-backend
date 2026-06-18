package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.NewsItem;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NewsMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsService {
    private static final int DEFAULT_LIMIT = 20;
    private final NewsMapper newsMapper;

    public List<NewsItem> findNews() {
        return newsMapper.findLatest(DEFAULT_LIMIT).stream()
                .map(row -> new NewsItem(
                        row.id(),
                        row.title(),
                        row.link(),
                        row.summary(),
                        row.source(),
                        row.publishedAt()
                ))
                .toList();
    }
}

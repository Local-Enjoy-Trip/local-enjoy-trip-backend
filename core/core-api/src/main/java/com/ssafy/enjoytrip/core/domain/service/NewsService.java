package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.NewsResult;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NewsMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsService {
    private static final int DEFAULT_LIMIT = 20;
    private final NewsMapper newsMapper;

    public List<NewsResult> findNews() {
        return newsMapper.findLatest(DEFAULT_LIMIT).stream()
                .map(record -> new NewsResult(
                        record.id(),
                        record.title(),
                        record.link(),
                        record.summary(),
                        record.source(),
                        record.publishedAt()
                ))
                .toList();
    }
}

package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.NewsResult;
import java.util.List;

public record NewsResponse(List<NewsResult> news) {
    public NewsResponse {
        news = List.copyOf(news);
    }
}

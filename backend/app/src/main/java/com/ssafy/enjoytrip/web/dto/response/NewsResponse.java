package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.NewsItem;
import java.util.List;

public record NewsResponse(List<NewsItem> news) {
}

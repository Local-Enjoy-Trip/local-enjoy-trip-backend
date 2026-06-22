package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.api.web.dto.response.NewsItem;
import java.util.List;

public record NewsResponse(List<NewsItem> news) {
}

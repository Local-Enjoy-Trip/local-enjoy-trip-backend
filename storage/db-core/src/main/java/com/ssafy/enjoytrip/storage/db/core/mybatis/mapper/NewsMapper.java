package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.mybatis.row.NewsItemRow;
import java.util.List;

public interface NewsMapper {
    List<NewsItemRow> findLatest(int limit);
}

package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.NewsItemRecord;
import java.util.List;

public interface NewsMapper {
    List<NewsItemRecord> findLatest(int limit);
}

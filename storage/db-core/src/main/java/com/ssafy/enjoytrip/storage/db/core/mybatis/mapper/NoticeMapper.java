package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.NoticeRecord;
import java.util.List;

public interface NoticeMapper {
    List<NoticeRecord> findAllOrderByCreatedAtDesc();

    NoticeRecord findById(Long id);

    int existsById(Long id);

    int insert(NoticeRecord record);

    int update(NoticeRecord record);

    int deleteById(Long id);
}

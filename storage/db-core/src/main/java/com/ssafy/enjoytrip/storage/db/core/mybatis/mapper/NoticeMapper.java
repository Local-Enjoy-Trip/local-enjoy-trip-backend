package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.entity.NoticeEntity;
import java.util.List;

public interface NoticeMapper {
    List<NoticeEntity> findAllOrderByCreatedAtDesc();

    NoticeEntity findById(Long id);

    int existsById(Long id);

    int insert(NoticeEntity entity);

    int update(NoticeEntity entity);

    int deleteById(Long id);
}

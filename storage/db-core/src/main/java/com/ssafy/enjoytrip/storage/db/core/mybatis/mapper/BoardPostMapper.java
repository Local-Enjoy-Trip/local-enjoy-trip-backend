package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.entity.BoardPostEntity;
import java.util.List;

public interface BoardPostMapper {
    List<BoardPostEntity> findAllOrderByCreatedAtDesc();

    BoardPostEntity findById(String id);

    int existsById(String id);

    int insert(BoardPostEntity entity);

    int update(BoardPostEntity entity);

    int deleteById(String id);
}

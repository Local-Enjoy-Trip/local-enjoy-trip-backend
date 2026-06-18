package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.BoardPostRecord;
import java.util.List;

public interface BoardPostMapper {
    List<BoardPostRecord> findAllOrderByCreatedAtDesc();

    BoardPostRecord findById(String id);

    int existsById(String id);

    int insert(BoardPostRecord record);

    int update(BoardPostRecord record);

    int deleteById(String id);
}

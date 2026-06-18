package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.AuthLogRecord;

public interface AuthLogMapper {
    int insert(AuthLogRecord record);
}

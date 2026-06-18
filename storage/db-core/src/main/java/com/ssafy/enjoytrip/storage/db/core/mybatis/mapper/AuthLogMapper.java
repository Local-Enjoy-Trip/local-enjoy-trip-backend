package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.entity.AuthLogEntity;

public interface AuthLogMapper {
    int insert(AuthLogEntity entity);
}

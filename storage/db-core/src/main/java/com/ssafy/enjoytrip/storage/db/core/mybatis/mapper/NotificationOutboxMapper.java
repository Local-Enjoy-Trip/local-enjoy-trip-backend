package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.entity.NotificationOutboxEntity;

public interface NotificationOutboxMapper {
    NotificationOutboxEntity findById(Long id);

    int insert(NotificationOutboxEntity entity);

    int markProcessed(NotificationOutboxEntity entity);

    int markFailed(NotificationOutboxEntity entity);
}

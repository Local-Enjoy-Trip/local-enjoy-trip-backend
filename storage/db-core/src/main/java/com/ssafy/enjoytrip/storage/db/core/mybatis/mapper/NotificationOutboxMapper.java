package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.NotificationOutboxRecord;

public interface NotificationOutboxMapper {
    NotificationOutboxRecord findById(Long id);

    int insert(NotificationOutboxRecord record);

    int markProcessed(NotificationOutboxRecord record);

    int markFailed(NotificationOutboxRecord record);
}

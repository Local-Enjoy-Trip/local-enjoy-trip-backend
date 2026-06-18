package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.HotplaceRecord;
import java.util.List;

public interface HotplaceMapper {
    List<HotplaceRecord> findAllOrderByCreatedAtDesc();

    List<HotplaceRecord> findByUserIdOrderByCreatedAtDesc(String userId);

    int existsById(String id);

    int insert(HotplaceRecord record);

    int deleteById(String id);
}

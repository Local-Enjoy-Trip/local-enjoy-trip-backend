package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.entity.HotplaceEntity;
import java.util.List;

public interface HotplaceMapper {
    List<HotplaceEntity> findAllOrderByCreatedAtDesc();

    List<HotplaceEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    int existsById(String id);

    int insert(HotplaceEntity entity);

    int deleteById(String id);
}

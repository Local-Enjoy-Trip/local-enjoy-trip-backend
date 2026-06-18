package com.ssafy.enjoytrip.storage.db.core.jpa;

import com.ssafy.enjoytrip.storage.db.core.entity.HotplaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotplaceJpaRepository extends JpaRepository<HotplaceEntity, String> {
    List<HotplaceEntity> findAllByOrderByCreatedAtDesc();

    List<HotplaceEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}

package com.ssafy.enjoytrip.storage.db.core.jpa;

import com.ssafy.enjoytrip.storage.db.core.entity.TravelPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelPlanJpaRepository extends JpaRepository<TravelPlanEntity, String> {
    List<TravelPlanEntity> findAllByOrderByCreatedAtDesc();

    List<TravelPlanEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}

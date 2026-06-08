package com.ssafy.enjoytrip.storage.jpa;

import com.ssafy.enjoytrip.storage.entity.TravelPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelPlanJpaRepository extends JpaRepository<TravelPlanEntity, String> {
    List<TravelPlanEntity> findAllByOrderByCreatedAtDesc();

    List<TravelPlanEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}

package com.ssafy.enjoytrip.storage.jpa;

import com.ssafy.enjoytrip.storage.entity.PlanItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanItemJpaRepository extends JpaRepository<PlanItemEntity, Long> {
    List<PlanItemEntity> findByPlanIdOrderByPositionAsc(String planId);

    void deleteByPlanId(String planId);
}

package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.entity.PlanItemEntity;
import com.ssafy.enjoytrip.storage.db.core.entity.TravelPlanEntity;
import com.ssafy.enjoytrip.storage.db.core.mybatis.row.AttractionRow;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PlanMapper {
    List<TravelPlanEntity> findAllOrderByCreatedAtDesc();

    List<TravelPlanEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    TravelPlanEntity findById(String id);

    int existsById(String id);

    int insertPlan(TravelPlanEntity entity);

    int updatePlan(TravelPlanEntity entity);

    int deletePlanById(String id);

    List<PlanItemEntity> findItemsByPlanIdOrderByPositionAsc(String planId);

    PlanItemEntity findItemById(Long id);

    int insertItem(PlanItemEntity entity);

    int deleteItemsByPlanId(String planId);

    int deleteItemById(Long id);

    List<AttractionRow> findAttractionsByIds(@Param("ids") List<Long> ids);
}

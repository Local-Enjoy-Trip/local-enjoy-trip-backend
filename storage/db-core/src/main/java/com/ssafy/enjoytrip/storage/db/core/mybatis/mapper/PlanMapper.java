package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.PlanItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.TravelPlanRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PlanMapper {
    List<TravelPlanRecord> findAllOrderByCreatedAtDesc();

    List<TravelPlanRecord> findByUserIdOrderByCreatedAtDesc(String userId);

    TravelPlanRecord findById(String id);

    int existsById(String id);

    int insertPlan(TravelPlanRecord record);

    int updatePlan(TravelPlanRecord record);

    int deletePlanById(String id);

    List<PlanItemRecord> findItemsByPlanIdOrderByPositionAsc(String planId);

    PlanItemRecord findItemById(Long id);

    int insertItem(PlanItemRecord record);

    int deleteItemsByPlanId(String planId);

    int deleteItemById(Long id);

    List<AttractionRecord> findAttractionsByIds(@Param("ids") List<Long> ids);
}

package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.PLAN_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.PlanItem;
import com.ssafy.enjoytrip.core.domain.PlanRouteItem;
import com.ssafy.enjoytrip.core.domain.TravelPlan;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.entity.PlanItemEntity;
import com.ssafy.enjoytrip.storage.db.core.entity.TravelPlanEntity;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.PlanMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.row.AttractionRow;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanMapper planMapper;

    public List<TravelPlan> findAllPlans() {
        return planMapper.findAllOrderByCreatedAtDesc().stream()
                .map(entity -> new TravelPlan(
                        entity.getId(),
                        entity.getUserId(),
                        entity.getTitle(),
                        entity.getStartDate(),
                        entity.getEndDate(),
                        entity.getBudget(),
                        entity.getNote(),
                        entity.getRouteItemsJson(),
                        stringValue(entity.getCreatedAt())
                ))
                .toList();
    }

    public List<TravelPlan> findPlansByUser(String userId) {
        return planMapper.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(entity -> new TravelPlan(
                        entity.getId(),
                        entity.getUserId(),
                        entity.getTitle(),
                        entity.getStartDate(),
                        entity.getEndDate(),
                        entity.getBudget(),
                        entity.getNote(),
                        entity.getRouteItemsJson(),
                        stringValue(entity.getCreatedAt())
                ))
                .toList();
    }

    public Optional<TravelPlan> findPlan(String id) {
        return Optional.ofNullable(planMapper.findById(id))
                .map(entity -> new TravelPlan(
                        entity.getId(),
                        entity.getUserId(),
                        entity.getTitle(),
                        entity.getStartDate(),
                        entity.getEndDate(),
                        entity.getBudget(),
                        entity.getNote(),
                        entity.getRouteItemsJson(),
                        stringValue(entity.getCreatedAt())
                ));
    }

    @Transactional
    public void createPlan(TravelPlan plan, List<PlanItem> routeItems) {
        savePlanWithItems(plan, routeItems);
    }

    @Transactional
    public void updatePlan(String authenticatedUserId,
                           String planId,
                           String title,
                           String startDate,
                           String endDate,
                           Integer budget,
                           String note,
                           List<PlanItem> routeItems) {
        TravelPlan current = requireOwnedPlan(planId, authenticatedUserId);
        TravelPlan next = current.merge(title, startDate, endDate, budget, note);
        boolean updated = routeItems == null ? updateStoredPlan(next) : updateStoredPlanWithItems(next, routeItems);
        if (!updated) {
            throw new CoreException(PLAN_NOT_FOUND);
        }
    }

    @Transactional
    public void replacePlanItems(String authenticatedUserId, String planId, List<PlanItem> items) {
        TravelPlan plan = requireOwnedPlan(planId, authenticatedUserId);
        if (!replaceStoredPlanItems(plan.id(), items)) {
            throw new CoreException(PLAN_NOT_FOUND);
        }
    }

    @Transactional
    public void deletePlanItem(String authenticatedUserId, String planId, Long itemId) {
        TravelPlan plan = requireOwnedPlan(planId, authenticatedUserId);
        if (!deleteStoredPlanItem(plan.id(), itemId)) {
            throw new CoreException(PLAN_NOT_FOUND);
        }
    }

    @Transactional
    public void deletePlan(String authenticatedUserId, String planId) {
        requireOwnedPlan(planId, authenticatedUserId);
        if (!deleteStoredPlan(planId)) {
            throw new CoreException(PLAN_NOT_FOUND);
        }
    }

    public void insertPlan(TravelPlan plan) {
        savePlan(plan);
    }

    @Transactional
    public void insertPlan(TravelPlan plan, List<PlanItem> items) {
        savePlanWithItems(plan, items);
    }

    @Transactional
    public boolean updatePlan(TravelPlan plan) {
        return updateStoredPlan(plan);
    }

    @Transactional
    public boolean updatePlan(TravelPlan plan, List<PlanItem> items) {
        return updateStoredPlanWithItems(plan, items);
    }

    @Transactional
    public boolean deletePlan(String id) {
        return deleteStoredPlan(id);
    }

    public List<PlanRouteItem> findPlanItems(String planId) {
        List<PlanItemEntity> items = planMapper.findItemsByPlanIdOrderByPositionAsc(planId);
        if (items.isEmpty()) {
            return List.of();
        }
        Map<Long, Attraction> attractions = findAttractions(items.stream()
                .map(PlanItemEntity::getAttractionId)
                .distinct()
                .toList());
        return items.stream()
                .map(item -> new PlanRouteItem(
                        item.getId(),
                        item.getAttractionId(),
                        String.valueOf(item.getId()),
                        item.getPosition(),
                        item.getDay(),
                        stringValue(item.getMemo()),
                        item.getStayMinutes(),
                        attractions.get(item.getAttractionId())
                ))
                .filter(item -> item.attraction() != null)
                .toList();
    }

    @Transactional
    public boolean replacePlanItems(String planId, List<PlanItem> items) {
        return replaceStoredPlanItems(planId, items);
    }

    @Transactional
    public boolean deletePlanItem(String planId, Long itemId) {
        return deleteStoredPlanItem(planId, itemId);
    }

    private TravelPlan requireOwnedPlan(String planId, String authenticatedUserId) {
        TravelPlan plan = findPlan(planId).orElseThrow(() -> new CoreException(PLAN_NOT_FOUND));
        plan.requireOwnedBy(authenticatedUserId);
        return plan;
    }

    private void savePlan(TravelPlan plan) {
        planMapper.insertPlan(new TravelPlanEntity(
                plan.id(),
                plan.userId(),
                plan.title(),
                plan.startDate(),
                plan.endDate(),
                plan.budget(),
                plan.note(),
                plan.routeItemsJson()
        ));
    }

    private void savePlanWithItems(TravelPlan plan, List<PlanItem> items) {
        savePlan(plan);
        replaceStoredPlanItems(plan.id(), items);
    }

    private boolean updateStoredPlan(TravelPlan plan) {
        TravelPlanEntity entity = planMapper.findById(plan.id());
        if (entity == null) {
            return false;
        }
        entity.update(
                plan.title(),
                plan.startDate(),
                plan.endDate(),
                plan.budget(),
                plan.note(),
                plan.routeItemsJson()
        );
        return planMapper.updatePlan(entity) > 0;
    }

    private boolean updateStoredPlanWithItems(TravelPlan plan, List<PlanItem> items) {
        if (!updateStoredPlan(plan)) {
            return false;
        }
        replaceStoredPlanItems(plan.id(), items);
        return true;
    }

    private boolean deleteStoredPlan(String id) {
        if (planMapper.existsById(id) <= 0) {
            return false;
        }
        return planMapper.deletePlanById(id) > 0;
    }

    private boolean replaceStoredPlanItems(String planId, List<PlanItem> items) {
        if (planMapper.existsById(planId) <= 0) {
            return false;
        }
        planMapper.deleteItemsByPlanId(planId);
        for (int index = 0; index < items.size(); index++) {
            PlanItem item = items.get(index);
            planMapper.insertItem(new PlanItemEntity(
                    planId,
                    item.attractionId(),
                    index + 1,
                    Math.max(1, item.day()),
                    item.memo(),
                    Math.max(1, item.stayMinutes())
            ));
        }
        return true;
    }

    private boolean deleteStoredPlanItem(String planId, Long itemId) {
        PlanItemEntity found = planMapper.findItemById(itemId);
        if (found == null || !found.getPlanId().equals(planId)) {
            return false;
        }
        List<PlanItem> remaining = planMapper.findItemsByPlanIdOrderByPositionAsc(planId).stream()
                .filter(item -> !item.getId().equals(itemId))
                .map(item -> new PlanItem(
                        item.getId(),
                        item.getPlanId(),
                        item.getAttractionId(),
                        item.getPosition(),
                        item.getDay(),
                        item.getMemo(),
                        item.getStayMinutes()
                ))
                .toList();
        planMapper.deleteItemById(itemId);
        replaceStoredPlanItems(planId, remaining);
        return true;
    }

    private Map<Long, Attraction> findAttractions(List<Long> attractionIds) {
        if (attractionIds.isEmpty()) {
            return Map.of();
        }
        return planMapper.findAttractionsByIds(attractionIds).stream()
                .map(row -> new Attraction(
                        row.id(),
                        row.title(),
                        row.addr1(),
                        row.addr2(),
                        row.zipcode(),
                        row.tel(),
                        row.firstImage(),
                        row.firstImage2(),
                        row.readCount(),
                        row.sidoCode(),
                        row.gugunCode(),
                        row.latitude(),
                        row.longitude(),
                        row.mlevel(),
                        row.contentTypeId(),
                        row.overview(),
                        0,
                        0.0,
                        0,
                        List.of(),
                        false,
                        null
                ))
                .collect(Collectors.toMap(Attraction::id, attraction -> attraction));
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}

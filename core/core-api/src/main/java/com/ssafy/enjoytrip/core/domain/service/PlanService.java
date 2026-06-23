package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.PLAN_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.PlanItem;
import com.ssafy.enjoytrip.core.domain.PlanRouteItem;
import com.ssafy.enjoytrip.core.domain.CoordinateRouteOrderOptimizer;

import com.ssafy.enjoytrip.core.domain.TravelPlan;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.PlanItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.TravelPlanRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.PlanMapper;

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
    private final CoordinateRouteOrderOptimizer routeOrderOptimizer;


    public List<TravelPlan> findAllPlans() {
        return planMapper.findAllOrderByCreatedAtDesc().stream()
                .map(record -> new TravelPlan(
                        record.getId(),
                        record.getUserId(),
                        record.getTitle(),
                        record.getStartDate(),
                        record.getEndDate(),
                        record.getBudget(),
                        record.getNote(),
                        record.getRouteItemsJson(),
                        stringValue(record.getCreatedAt())
                ))
                .toList();
    }

    public List<TravelPlan> findPlansByUser(String userId) {
        return planMapper.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(record -> new TravelPlan(
                        record.getId(),
                        record.getUserId(),
                        record.getTitle(),
                        record.getStartDate(),
                        record.getEndDate(),
                        record.getBudget(),
                        record.getNote(),
                        record.getRouteItemsJson(),
                        stringValue(record.getCreatedAt())
                ))
                .toList();
    }

    public TravelPlan findRequiredPlan(String id) {
        return findPlan(id).orElseThrow(() -> new CoreException(PLAN_NOT_FOUND));
    }

    public Optional<TravelPlan> findPlan(String id) {
        return Optional.ofNullable(planMapper.findById(id))
                .map(record -> new TravelPlan(
                        record.getId(),
                        record.getUserId(),
                        record.getTitle(),
                        record.getStartDate(),
                        record.getEndDate(),
                        record.getBudget(),
                        record.getNote(),
                        record.getRouteItemsJson(),
                        stringValue(record.getCreatedAt())
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
        boolean updated = routeItems == null
                ? updateStoredPlan(next)
                : updateStoredPlanWithItems(next, routeItems);
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
        List<PlanItemRecord> items = planMapper.findItemsByPlanIdOrderByPositionAsc(planId);
        if (items.isEmpty()) {
            return List.of();
        }
        Map<Long, Attraction> attractions = findAttractions(items.stream()
                .map(PlanItemRecord::getAttractionId)
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
        TravelPlan plan = findRequiredPlan(planId);
        plan.requireOwnedBy(authenticatedUserId);
        return plan;
    }

    private void savePlan(TravelPlan plan) {
        planMapper.insertPlan(new TravelPlanRecord(
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
        TravelPlanRecord record = planMapper.findById(plan.id());
        if (record == null) {
            return false;
        }
        record.update(
                plan.title(),
                plan.startDate(),
                plan.endDate(),
                plan.budget(),
                plan.note(),
                plan.routeItemsJson()
        );
        return planMapper.updatePlan(record) > 0;
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
        List<PlanItem> optimizedItems = optimizeRouteItems(items);

        planMapper.deleteItemsByPlanId(planId);
        for (int index = 0; index < optimizedItems.size(); index++) {
            PlanItem item = optimizedItems.get(index);
            planMapper.insertItem(new PlanItemRecord(
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
        PlanItemRecord found = planMapper.findItemById(itemId);
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

    private List<PlanItem> optimizeRouteItems(List<PlanItem> items) {
        if (items == null || items.size() < 2) {
            return items == null ? List.of() : items;
        }

        Map<Long, Attraction> attractions = findAttractions(items.stream()
                .map(PlanItem::attractionId)
                .distinct()
                .toList());
        return routeOrderOptimizer.optimizeByDay(
                items,
                PlanItem::day,
                item -> latitudeOf(item, attractions),
                item -> longitudeOf(item, attractions)
        );
    }

    private static Double latitudeOf(PlanItem item, Map<Long, Attraction> attractions) {
        Attraction attraction = attractions.get(item.attractionId());
        return attraction == null ? null : attraction.latitude();
    }

    private static Double longitudeOf(PlanItem item, Map<Long, Attraction> attractions) {
        Attraction attraction = attractions.get(item.attractionId());
        return attraction == null ? null : attraction.longitude();
    }

    private Map<Long, Attraction> findAttractions(List<Long> attractionIds) {
        if (attractionIds.isEmpty()) {
            return Map.of();
        }
        return planMapper.findAttractionsByIds(attractionIds).stream()
                .map(record -> new Attraction(
                        record.id(),
                        record.title(),
                        record.addr1(),
                        record.addr2(),
                        record.zipcode(),
                        record.tel(),
                        record.firstImage(),
                        record.firstImage2(),
                        record.readCount(),
                        record.sidoCode(),
                        record.gugunCode(),
                        record.latitude(),
                        record.longitude(),
                        record.mlevel(),
                        record.contentTypeId(),
                        record.overview(),
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

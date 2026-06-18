package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.PLAN_NOT_FOUND;
import static com.ssafy.enjoytrip.storage.db.core.jooq.tables.Attractions.ATTRACTIONS;
import static org.jooq.impl.DSL.field;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.PlanItem;
import com.ssafy.enjoytrip.core.domain.PlanRouteItem;
import com.ssafy.enjoytrip.core.domain.TravelPlan;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.entity.PlanItemEntity;
import com.ssafy.enjoytrip.storage.db.core.entity.TravelPlanEntity;
import com.ssafy.enjoytrip.storage.db.core.jpa.PlanItemJpaRepository;
import com.ssafy.enjoytrip.storage.db.core.jpa.TravelPlanJpaRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanService {

    public List<TravelPlan> findAllPlans() {
        return jpaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
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
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
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
        return jpaRepository.findById(id)
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
    public void updatePlan(
            String authenticatedUserId,
            String planId,
            String title,
            String startDate,
            String endDate,
            Integer budget,
            String note,
            List<PlanItem> routeItems
    ) {
        TravelPlan current = requireOwnedPlan(planId, authenticatedUserId);
        TravelPlan next = current.merge(
                title,
                startDate,
                endDate,
                budget,
                note
        );
        boolean updated = routeItems == null
                ? updateStoredPlan(next)
                : updateStoredPlanWithItems(next, routeItems);
        if (!updated) {
            throw new CoreException(PLAN_NOT_FOUND);
        }
    }

    @Transactional
    public void replacePlanItems(
            String authenticatedUserId,
            String planId,
            List<PlanItem> items
    ) {
        TravelPlan plan = requireOwnedPlan(planId, authenticatedUserId);
        if (!replaceStoredPlanItems(plan.id(), items)) {
            throw new CoreException(PLAN_NOT_FOUND);
        }
    }

    private TravelPlan requireOwnedPlan(String planId, String authenticatedUserId) {
        TravelPlan plan = jpaRepository.findById(planId)
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
                .orElseThrow(() -> new CoreException(PLAN_NOT_FOUND));
        plan.requireOwnedBy(authenticatedUserId);

        return plan;
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
        List<PlanItemEntity> items = itemJpaRepository.findByPlanIdOrderByPositionAsc(planId);
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

    private void savePlan(TravelPlan plan) {
        jpaRepository.save(new TravelPlanEntity(
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
        return jpaRepository.findById(plan.id())
                .map(entity -> {
                    entity.update(
                            plan.title(),
                            plan.startDate(),
                            plan.endDate(),
                            plan.budget(),
                            plan.note(),
                            plan.routeItemsJson()
                    );
                    return true;
                })
                .orElse(false);
    }

    private boolean updateStoredPlanWithItems(TravelPlan plan, List<PlanItem> items) {
        if (!updateStoredPlan(plan)) {
            return false;
        }
        replaceStoredPlanItems(plan.id(), items);
        return true;
    }

    private boolean deleteStoredPlan(String id) {
        if (!jpaRepository.existsById(id)) {
            return false;
        }
        jpaRepository.deleteById(id);
        return true;
    }

    private boolean replaceStoredPlanItems(String planId, List<PlanItem> items) {
        if (!jpaRepository.existsById(planId)) {
            return false;
        }
        itemJpaRepository.deleteByPlanId(planId);
        itemJpaRepository.flush();
        for (int index = 0; index < items.size(); index++) {
            PlanItem item = items.get(index);
            itemJpaRepository.save(new PlanItemEntity(
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
        Optional<PlanItemEntity> found = itemJpaRepository.findById(itemId)
                .filter(item -> item.getPlanId().equals(planId));
        if (found.isEmpty()) {
            return false;
        }
        List<PlanItem> remaining = itemJpaRepository.findByPlanIdOrderByPositionAsc(planId).stream()
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
        itemJpaRepository.deleteById(itemId);
        itemJpaRepository.flush();
        replaceStoredPlanItems(planId, remaining);
        return true;
    }

    private final TravelPlanJpaRepository jpaRepository;
    private final PlanItemJpaRepository itemJpaRepository;
    private final DSLContext dslContext;


    private Map<Long, Attraction> findAttractions(List<Long> attractionIds) {
        return dslContext.select(
                        ATTRACTIONS.ID,
                        ATTRACTIONS.TITLE,
                        ATTRACTIONS.ADDR1,
                        ATTRACTIONS.ADDR2,
                        ATTRACTIONS.ZIPCODE,
                        ATTRACTIONS.TEL,
                        ATTRACTIONS.FIRST_IMAGE.as("firstImage"),
                        ATTRACTIONS.FIRST_IMAGE2.as("firstImage2"),
                        ATTRACTIONS.READ_COUNT.as("readcount"),
                        ATTRACTIONS.SIDO_CODE.as("sidoCode"),
                        ATTRACTIONS.GUGUN_CODE.as("gugunCode"),
                        field("ST_Y({0})", Double.class, ATTRACTIONS.LOCATION).as("latitude"),
                        field("ST_X({0})", Double.class, ATTRACTIONS.LOCATION).as("longitude"),
                        ATTRACTIONS.MLEVEL,
                        ATTRACTIONS.CONTENT_TYPE_ID.as("contentTypeId"),
                        ATTRACTIONS.OVERVIEW
                )
                .from(ATTRACTIONS)
                .where(ATTRACTIONS.ID.in(attractionIds))
                .fetch(record -> new Attraction(
                        record.get(ATTRACTIONS.ID),
                        record.get(ATTRACTIONS.TITLE),
                        record.get(ATTRACTIONS.ADDR1),
                        record.get(ATTRACTIONS.ADDR2),
                        record.get(ATTRACTIONS.ZIPCODE),
                        record.get(ATTRACTIONS.TEL),
                        record.get("firstImage", String.class),
                        record.get("firstImage2", String.class),
                        record.get("readcount", Integer.class),
                        record.get("sidoCode", Integer.class),
                        record.get("gugunCode", Integer.class),
                        record.get("latitude", Double.class),
                        record.get("longitude", Double.class),
                        record.get(ATTRACTIONS.MLEVEL),
                        record.get("contentTypeId", String.class),
                        record.get(ATTRACTIONS.OVERVIEW),
                        0,
                        0.0,
                        0,
                        List.of(),
                        false,
                        null
                ))
                .stream()
                .collect(Collectors.toMap(Attraction::id, attraction -> attraction));
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}

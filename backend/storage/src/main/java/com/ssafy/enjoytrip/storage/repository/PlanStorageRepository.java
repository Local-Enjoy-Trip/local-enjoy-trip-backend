package com.ssafy.enjoytrip.storage.repository;

import static com.ssafy.enjoytrip.storage.jooq.tables.Attractions.ATTRACTIONS;
import static org.jooq.impl.DSL.field;

import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.domain.PlanItem;
import com.ssafy.enjoytrip.domain.PlanRouteItem;
import com.ssafy.enjoytrip.domain.TravelPlan;
import com.ssafy.enjoytrip.repository.PlanRepository;
import com.ssafy.enjoytrip.storage.entity.PlanItemEntity;
import com.ssafy.enjoytrip.storage.entity.TravelPlanEntity;
import com.ssafy.enjoytrip.storage.jpa.PlanItemJpaRepository;
import com.ssafy.enjoytrip.storage.jpa.TravelPlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PlanStorageRepository implements PlanRepository {
    private final TravelPlanJpaRepository jpaRepository;
    private final PlanItemJpaRepository itemJpaRepository;
    private final DSLContext dslContext;

    @Override
    public List<TravelPlan> findAll() {
        return jpaRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toModel).toList();
    }

    @Override
    public List<TravelPlan> findByUser(String userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toModel).toList();
    }

    @Override
    public Optional<TravelPlan> findById(String id) {
        return jpaRepository.findById(id).map(this::toModel);
    }

    @Override
    @Transactional
    public void insert(TravelPlan plan) {
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

    @Override
    @Transactional
    public void insert(TravelPlan plan, List<PlanItem> items) {
        insert(plan);
        replaceItems(plan.id(), items);
    }

    @Override
    @Transactional
    public boolean update(TravelPlan plan) {
        return jpaRepository.findById(plan.id())
                .map(entity -> {
                    entity.update(plan.title(), plan.startDate(), plan.endDate(), plan.budget(), plan.note(), plan.routeItemsJson());
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean update(TravelPlan plan, List<PlanItem> items) {
        if (!update(plan)) {
            return false;
        }
        replaceItems(plan.id(), items);
        return true;
    }

    @Override
    @Transactional
    public boolean delete(String id) {
        if (!jpaRepository.existsById(id)) {
            return false;
        }
        jpaRepository.deleteById(id);
        return true;
    }

    @Override
    public List<PlanRouteItem> findItems(String planId) {
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

    @Override
    @Transactional
    public boolean replaceItems(String planId, List<PlanItem> items) {
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

    @Override
    @Transactional
    public boolean deleteItem(String planId, Long itemId) {
        Optional<PlanItemEntity> found = itemJpaRepository.findById(itemId)
                .filter(item -> item.getPlanId().equals(planId));
        if (found.isEmpty()) {
            return false;
        }
        List<PlanItem> remaining = itemJpaRepository.findByPlanIdOrderByPositionAsc(planId).stream()
                .filter(item -> !item.getId().equals(itemId))
                .map(item -> new PlanItem(item.getId(), item.getPlanId(), item.getAttractionId(), item.getPosition(),
                        item.getDay(), item.getMemo(), item.getStayMinutes()))
                .toList();
        itemJpaRepository.deleteById(itemId);
        itemJpaRepository.flush();
        replaceItems(planId, remaining);
        return true;
    }

    private TravelPlan toModel(TravelPlanEntity entity) {
        return new TravelPlan(
                entity.getId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getBudget(),
                entity.getNote(),
                entity.getRouteItemsJson(),
                stringValue(entity.getCreatedAt())
        );
    }

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

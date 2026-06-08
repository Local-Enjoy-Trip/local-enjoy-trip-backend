package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.TravelPlan;
import com.ssafy.enjoytrip.domain.PlanItem;
import com.ssafy.enjoytrip.domain.PlanRouteItem;

import java.util.List;
import java.util.Optional;

public interface PlanRepository {
    List<TravelPlan> findAll();

    List<TravelPlan> findByUser(String userId);

    Optional<TravelPlan> findById(String id);

    void insert(TravelPlan plan);

    default void insert(TravelPlan plan, List<PlanItem> items) {
        insert(plan);
        replaceItems(plan.id(), items);
    }

    boolean update(TravelPlan plan);

    default boolean update(TravelPlan plan, List<PlanItem> items) {
        if (!update(plan)) {
            return false;
        }
        replaceItems(plan.id(), items);
        return true;
    }

    boolean delete(String id);

    List<PlanRouteItem> findItems(String planId);

    boolean replaceItems(String planId, List<PlanItem> items);

    boolean deleteItem(String planId, Long itemId);
}

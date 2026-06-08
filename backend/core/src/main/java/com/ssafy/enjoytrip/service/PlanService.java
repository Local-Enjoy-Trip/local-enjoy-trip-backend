package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.TravelPlan;
import com.ssafy.enjoytrip.domain.PlanItem;
import com.ssafy.enjoytrip.domain.PlanRouteItem;
import com.ssafy.enjoytrip.repository.PlanRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository repository;

    public List<TravelPlan> findAllPlans() {
        return repository.findAll();
    }

    public List<TravelPlan> findPlansByUser(String userId) {
        return repository.findByUser(userId);
    }

    public Optional<TravelPlan> findPlan(String id) {
        return repository.findById(id);
    }

    public void insertPlan(TravelPlan plan) {
        repository.insert(plan);
    }

    public void insertPlan(TravelPlan plan, List<PlanItem> items) {
        repository.insert(plan, items);
    }

    public boolean updatePlan(TravelPlan plan) {
        return repository.update(plan);
    }

    public boolean updatePlan(TravelPlan plan, List<PlanItem> items) {
        return repository.update(plan, items);
    }

    public boolean deletePlan(String id) {
        return repository.delete(id);
    }

    public List<PlanRouteItem> findPlanItems(String planId) {
        return repository.findItems(planId);
    }

    public boolean replacePlanItems(String planId, List<PlanItem> items) {
        return repository.replaceItems(planId, items);
    }

    public boolean deletePlanItem(String planId, Long itemId) {
        return repository.deleteItem(planId, itemId);
    }
}

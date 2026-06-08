package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.support.error.ErrorType.ACCESS_DENIED;
import static com.ssafy.enjoytrip.support.error.ErrorType.PLAN_NOT_FOUND;

import com.ssafy.enjoytrip.domain.PlanItem;
import com.ssafy.enjoytrip.domain.PlanRouteItem;
import com.ssafy.enjoytrip.domain.TravelPlan;
import com.ssafy.enjoytrip.repository.PlanRepository;
import com.ssafy.enjoytrip.service.command.PlanMutationCommand;
import com.ssafy.enjoytrip.service.command.PlanRouteItemCommand;
import com.ssafy.enjoytrip.support.error.CoreException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public void createPlan(String authenticatedUserId, PlanMutationCommand command) {
        TravelPlan plan = new TravelPlan(
                command.id(),
                authenticatedUserId,
                command.title(),
                command.startDate(),
                command.endDate(),
                value(command.budget(), 0),
                stringValue(command.note()),
                "[]",
                ""
        );
        repository.insert(plan, toPlanItems(command.id(), command.routeItems()));
    }

    public void updatePlan(String authenticatedUserId, String planId, PlanMutationCommand command) {
        TravelPlan current = requireOwnedPlan(planId, authenticatedUserId);
        TravelPlan next = new TravelPlan(
                current.id(),
                current.userId(),
                defaultValue(command.title(), current.title()),
                defaultValue(command.startDate(), current.startDate()),
                defaultValue(command.endDate(), current.endDate()),
                value(command.budget(), current.budget()),
                defaultValue(command.note(), current.note()),
                current.routeItemsJson(),
                current.createdAt()
        );
        boolean updated = command.routeItems() == null
                ? repository.update(next)
                : repository.update(next, toPlanItems(current.id(), command.routeItems()));
        if (!updated) {
            throw new CoreException(PLAN_NOT_FOUND);
        }
    }

    public void replacePlanItems(
            String authenticatedUserId,
            String planId,
            List<PlanRouteItemCommand> items
    ) {
        TravelPlan plan = requireOwnedPlan(planId, authenticatedUserId);
        if (!repository.replaceItems(plan.id(), toPlanItems(plan.id(), items))) {
            throw new CoreException(PLAN_NOT_FOUND);
        }
    }

    public void deletePlanItem(String authenticatedUserId, String planId, Long itemId) {
        TravelPlan plan = requireOwnedPlan(planId, authenticatedUserId);
        if (!repository.deleteItem(plan.id(), itemId)) {
            throw new CoreException(PLAN_NOT_FOUND);
        }
    }

    public void deletePlan(String authenticatedUserId, String planId) {
        requireOwnedPlan(planId, authenticatedUserId);
        if (!repository.delete(planId)) {
            throw new CoreException(PLAN_NOT_FOUND);
        }
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

    private TravelPlan requireOwnedPlan(String planId, String authenticatedUserId) {
        TravelPlan plan = repository.findById(planId)
                .orElseThrow(() -> new CoreException(PLAN_NOT_FOUND));
        if (!plan.userId().equals(authenticatedUserId)) {
            throw new CoreException(ACCESS_DENIED);
        }
        return plan;
    }

    private static List<PlanItem> toPlanItems(String planId, List<PlanRouteItemCommand> routeItems) {
        if (routeItems == null || routeItems.isEmpty()) {
            return List.of();
        }
        return routeItems.stream()
                .map(item -> new PlanItem(
                        null,
                        planId,
                        item.attractionId(),
                        0,
                        value(item.day(), 1),
                        stringValue(item.memo()),
                        value(item.stayMinutes(), 90)
                ))
                .toList();
    }

    private static String defaultValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.strip();
    }

    private static String stringValue(String value) {
        if (value == null) {
            return "";
        }
        return value.strip();
    }

    private static int value(Integer value, int fallback) {
        if (value == null) {
            return fallback;
        }
        return value;
    }
}

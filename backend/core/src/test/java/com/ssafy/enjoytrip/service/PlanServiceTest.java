package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.support.error.ErrorType.ACCESS_DENIED;
import static com.ssafy.enjoytrip.support.error.ErrorType.PLAN_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ssafy.enjoytrip.domain.PlanItem;
import com.ssafy.enjoytrip.domain.PlanRouteItem;
import com.ssafy.enjoytrip.domain.TravelPlan;
import com.ssafy.enjoytrip.repository.PlanRepository;
import com.ssafy.enjoytrip.service.command.PlanMutationCommand;
import com.ssafy.enjoytrip.service.command.PlanRouteItemCommand;
import com.ssafy.enjoytrip.support.error.CoreException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PlanServiceTest {

    @Test
    void createPlanUsesAuthenticatedUserAndNormalizesRouteItems() {
        FakePlanRepository repository = new FakePlanRepository();
        PlanService service = new PlanService(repository);

        service.createPlan("ssafy", new PlanMutationCommand(
                "p1",
                "서울 여행",
                "2026-05-14",
                "2026-05-15",
                null,
                null,
                List.of(new PlanRouteItemCommand(10L, 2, " lunch ", 120))
        ));

        assertEquals("ssafy", repository.savedPlan.userId());
        assertEquals(0, repository.savedPlan.budget());
        assertTrue(repository.savedPlan.note().isEmpty());
        assertEquals(List.of(new PlanItem(null, "p1", 10L, 0, 2, "lunch", 120)), repository.savedItems);
    }

    @Test
    void updatePlanWithoutRouteItemsKeepsExistingItemsAndEmptyRouteItemsExplicitlyReplacesThem() {
        FakePlanRepository repository = new FakePlanRepository();
        repository.plans.put("p1", new TravelPlan(
                "p1",
                "owner",
                "서울",
                "2026-05-14",
                "2026-05-15",
                1000,
                "note",
                "[]",
                "created"
        ));
        PlanService service = new PlanService(repository);

        service.updatePlan("owner", "p1", new PlanMutationCommand(
                null,
                " 부산 ",
                null,
                null,
                2000,
                null,
                null
        ));

        assertEquals("부산", repository.plans.get("p1").title());
        assertEquals(2000, repository.plans.get("p1").budget());
        assertEquals("note", repository.plans.get("p1").note());
        assertEquals(1, repository.updatePlanCallCount);
        assertEquals(0, repository.updatePlanWithItemsCallCount);

        service.updatePlan("owner", "p1", new PlanMutationCommand(
                null,
                null,
                null,
                null,
                null,
                null,
                List.of()
        ));

        assertEquals(1, repository.updatePlanCallCount);
        assertEquals(1, repository.updatePlanWithItemsCallCount);
        assertTrue(repository.savedItems.isEmpty());
    }

    @Test
    void updatePlanChecksOwnershipInsideService() {
        FakePlanRepository repository = new FakePlanRepository();
        repository.plans.put("p1", new TravelPlan(
                "p1",
                "owner",
                "서울",
                "2026-05-14",
                "2026-05-15",
                1000,
                "note",
                "[]",
                "created"
        ));
        PlanService service = new PlanService(repository);

        CoreException exception = assertThrows(CoreException.class, () -> service.updatePlan(
                "other",
                "p1",
                new PlanMutationCommand(null, "제주", null, null, null, null, null)
        ));
        assertEquals(ACCESS_DENIED, exception.errorType());
    }

    @Test
    void deleteMissingPlanRaisesNotFound() {
        PlanService service = new PlanService(new FakePlanRepository());

        CoreException exception = assertThrows(
                CoreException.class,
                () -> service.deletePlan("ssafy", "missing")
        );
        assertEquals(PLAN_NOT_FOUND, exception.errorType());
    }

    private static class FakePlanRepository implements PlanRepository {
        private final Map<String, TravelPlan> plans = new HashMap<>();
        private TravelPlan savedPlan;
        private List<PlanItem> savedItems = List.of();
        private int updatePlanCallCount;
        private int updatePlanWithItemsCallCount;

        @Override
        public List<TravelPlan> findAll() {
            return new ArrayList<>(plans.values());
        }

        @Override
        public List<TravelPlan> findByUser(String userId) {
            return plans.values().stream().filter(plan -> plan.userId().equals(userId)).toList();
        }

        @Override
        public Optional<TravelPlan> findById(String id) {
            return Optional.ofNullable(plans.get(id));
        }

        @Override
        public void insert(TravelPlan plan) {
            savedPlan = plan;
            plans.put(plan.id(), plan);
        }

        @Override
        public void insert(TravelPlan plan, List<PlanItem> items) {
            insert(plan);
            savedItems = items;
        }

        @Override
        public boolean update(TravelPlan plan) {
            updatePlanCallCount++;
            if (!plans.containsKey(plan.id())) {
                return false;
            }
            plans.put(plan.id(), plan);
            return true;
        }

        @Override
        public boolean update(TravelPlan plan, List<PlanItem> items) {
            updatePlanWithItemsCallCount++;
            if (!plans.containsKey(plan.id())) {
                return false;
            }
            plans.put(plan.id(), plan);
            savedItems = items;
            return true;
        }

        @Override
        public boolean delete(String id) {
            return plans.remove(id) != null;
        }

        @Override
        public List<PlanRouteItem> findItems(String planId) {
            return List.of();
        }

        @Override
        public boolean replaceItems(String planId, List<PlanItem> items) {
            if (!plans.containsKey(planId)) {
                return false;
            }
            savedItems = items;
            return true;
        }

        @Override
        public boolean deleteItem(String planId, Long itemId) {
            return plans.containsKey(planId);
        }
    }
}

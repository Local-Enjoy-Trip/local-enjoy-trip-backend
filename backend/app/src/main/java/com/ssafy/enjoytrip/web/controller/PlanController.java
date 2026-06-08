package com.ssafy.enjoytrip.web.controller;

import com.ssafy.enjoytrip.web.api.*;

import static com.ssafy.enjoytrip.support.error.ErrorType.ACCESS_DENIED;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_ACTION;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_ID;
import static com.ssafy.enjoytrip.support.error.ErrorType.MISSING_ID;
import static com.ssafy.enjoytrip.support.error.ErrorType.MISSING_REQUIRED_FIELDS;
import static com.ssafy.enjoytrip.support.error.ErrorType.PLAN_NOT_FOUND;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.PlanItem;
import com.ssafy.enjoytrip.domain.TravelPlan;
import com.ssafy.enjoytrip.service.PlanService;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.error.ErrorType;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.util.Checked;
import com.ssafy.enjoytrip.web.dto.request.PlanItemsRequest;
import com.ssafy.enjoytrip.web.dto.request.PlanRequest;
import com.ssafy.enjoytrip.web.dto.response.PlanResponse;
import com.ssafy.enjoytrip.web.dto.response.PlanRouteItemResponse;
import com.ssafy.enjoytrip.web.dto.response.PlansResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController implements PlanApi {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PlanService service;

    @GetMapping
    @Override
    public ApiResponse<PlansResponse> find(@RequestParam(required = false) String userId) {
        String trimmedUserId = trim(userId);
        if (trimmedUserId.isEmpty()) {
            List<PlanResponse> plans = service.findAllPlans().stream().map(this::toResponse).toList();
            return success(new PlansResponse(plans));
        }
        List<PlanResponse> plans = service.findPlansByUser(trimmedUserId).stream().map(this::toResponse).toList();
        return success(new PlansResponse(plans));
    }

    @GetMapping("/{id}")
    @Override
    public ApiResponse<PlanResponse> findOne(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        return service.findPlan(trim(id))
                .map(plan -> success(toResponse(plan)))
                .orElseGet(() -> fail(PLAN_NOT_FOUND));
    }

    @PostMapping
    @Override
    public ApiResponse<Void> legacyPost(@ModelAttribute PlanRequest request, @AuthenticationPrincipal Jwt jwt) {
        return switch (trim(request.action())) {
            case "create" -> create(request, jwt);
            case "delete" -> delete(trim(request.id()), jwt);
            default -> fail(INVALID_ACTION);
        };
    }

    @PostMapping("/items")
    @Override
    public ApiResponse<Void> create(@ModelAttribute PlanRequest request, @AuthenticationPrincipal Jwt jwt) {
        String id = trim(request.id());
        String userId = authenticatedUserId(jwt);
        String title = trim(request.title());
        String startDate = trim(request.startDate());
        String endDate = trim(request.endDate());
        if (id.isEmpty() || userId.isEmpty() || title.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            return fail(MISSING_REQUIRED_FIELDS);
        }
        String routeItems = trim(request.routeItems());
        service.insertPlan(new TravelPlan(id, userId, title, startDate, endDate,
                parseInt(request.budget(), 0), trim(request.note()),
                routeItemsOrDefault(routeItems), ""), parsePlanItems(id, routeItems));
        return success();
    }

    @PutMapping("/{id}")
    @Override
    public ApiResponse<Void> update(@PathVariable String id,
                                    @ModelAttribute PlanRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        TravelPlan plan = requireOwnedPlan(id, jwt);
        String title = defaultValue(trim(request.title()), plan.title());
        String startDate = defaultValue(trim(request.startDate()), plan.startDate());
        String endDate = defaultValue(trim(request.endDate()), plan.endDate());
        String routeItems = trim(request.routeItems());
        TravelPlan next = new TravelPlan(plan.id(), plan.userId(), title, startDate, endDate,
                parseInt(request.budget(), plan.budget()), defaultValue(trim(request.note()), plan.note()),
                routeItems.isEmpty() ? plan.routeItemsJson() : routeItems, plan.createdAt());
        if (!routeItems.isEmpty()) {
            if (!service.updatePlan(next, parsePlanItems(plan.id(), routeItems))) {
                return fail(PLAN_NOT_FOUND);
            }
            return success();
        }
        if (!service.updatePlan(next)) {
            return fail(PLAN_NOT_FOUND);
        }
        return success();
    }

    @PutMapping("/{id}/items")
    @Override
    public ApiResponse<Void> replaceItems(@PathVariable String id,
                                          @ModelAttribute PlanItemsRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        TravelPlan plan = requireOwnedPlan(id, jwt);
        if (!service.replacePlanItems(plan.id(), parsePlanItems(plan.id(), trim(request.routeItems())))) {
            return fail(PLAN_NOT_FOUND);
        }
        return success();
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Override
    public ApiResponse<Void> deleteItem(@PathVariable String id,
                                        @PathVariable Long itemId,
                                        @AuthenticationPrincipal Jwt jwt) {
        TravelPlan plan = requireOwnedPlan(id, jwt);
        if (itemId == null || itemId <= 0) {
            return fail(INVALID_ID);
        }
        if (!service.deletePlanItem(plan.id(), itemId)) {
            return fail(PLAN_NOT_FOUND);
        }
        return success();
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        if (trim(id).isEmpty()) {
            return fail(MISSING_ID);
        }
        requireOwnedPlan(id, jwt);
        if (service.deletePlan(id)) {
            return success();
        }
        return fail(PLAN_NOT_FOUND);
    }

    private PlanResponse toResponse(TravelPlan plan) {
        List<PlanRouteItemResponse> normalizedItems = service.findPlanItems(plan.id()).stream()
                .map(PlanRouteItemResponse::from)
                .toList();
        return new PlanResponse(
                plan.id(),
                plan.userId(),
                plan.title(),
                plan.startDate(),
                plan.endDate(),
                plan.budget(),
                value(plan.note(), ""),
                normalizedItems.isEmpty() ? parseJsonOrEmptyList(plan.routeItemsJson()) : normalizedItems,
                value(plan.createdAt(), "")
        );
    }

    private TravelPlan requireOwnedPlan(String id, Jwt jwt) {
        String trimmedId = trim(id);
        if (trimmedId.isEmpty()) {
            return fail(MISSING_ID);
        }
        TravelPlan plan = service.findPlan(trimmedId).orElseGet(() -> fail(PLAN_NOT_FOUND));
        String userId = authenticatedUserId(jwt);
        if (!plan.userId().equals(userId)) {
            return fail(ACCESS_DENIED);
        }
        return plan;
    }

    private static List<PlanItem> parsePlanItems(String planId, String raw) {
        String value = trim(raw);
        if (value.isEmpty() || !looksLikeJsonArray(value)) {
            return List.of();
        }
        return Checked.getOrElse(
                () -> {
                    RouteItemPayload[] rows = OBJECT_MAPPER.readValue(value, RouteItemPayload[].class);
                    java.util.ArrayList<PlanItem> items = new java.util.ArrayList<>();
                    for (int index = 0; index < rows.length; index++) {
                        RouteItemPayload row = rows[index];
                        Long attractionId = firstLong(row.attractionId(), row.id(), row.contentId());
                        if (attractionId == null) {
                            continue;
                        }
                        items.add(new PlanItem(
                                null,
                                planId,
                                attractionId,
                                index + 1,
                                Math.max(1, intValue(row.day(), 1)),
                                objectString(row.memo()),
                                Math.max(1, intValue(row.stayMinutes(), 90))
                        ));
                    }
                    return items;
                },
                () -> List.of()
        );
    }

    private static <T> T fail(ErrorType error) {
        throw new CoreException(error);
    }

    private static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static int parseInt(String raw, int fallback) {
        String value = trim(raw);
        if (value.isEmpty()) {
            return fallback;
        }
        if (!isInteger(value)) {
            return fallback;
        }
        return Integer.parseInt(value);
    }

    private static boolean isInteger(String value) {
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (i == 0 && current == '-') {
                continue;
            }
            if (!Character.isDigit(current)) {
                return false;
            }
        }
        return !value.equals("-");
    }

    private static String value(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        return value;
    }

    private static String routeItemsOrDefault(String routeItems) {
        if (routeItems.isEmpty()) {
            return "[]";
        }
        return routeItems;
    }

    private static Object parseJsonOrEmptyList(String raw) {
        String value = trim(raw);
        if (value.isEmpty()) {
            return List.of();
        }
        if (!looksLikeJsonArray(value)) {
            return List.of();
        }
        return Checked.getOrElse(
                () -> OBJECT_MAPPER.readValue(value, Object.class),
                () -> List.of()
        );
    }

    private static boolean looksLikeJsonArray(String value) {
        if (!value.startsWith("[")) {
            return false;
        }
        return value.endsWith("]");
    }

    private String authenticatedUserId(Jwt jwt) {
        return trim(jwt.getSubject());
    }

    private static String defaultValue(String value, String fallback) {
        if (value.isEmpty()) {
            return fallback;
        }
        return value;
    }

    private static Long firstLong(Object... values) {
        for (Object value : values) {
            Long parsed = longValue(value);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private static Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            String text = trim(String.valueOf(value));
            if (text.isEmpty()) {
                return null;
            }
            return Long.parseLong(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static int intValue(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        return parseInt(String.valueOf(value), fallback);
    }

    private static String objectString(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }

    private record RouteItemPayload(
            Object id,
            Object attractionId,
            Object contentId,
            Object day,
            Object memo,
            Object stayMinutes
    ) {
    }

}

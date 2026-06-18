package com.ssafy.enjoytrip.core.api.web.mapper;

import com.ssafy.enjoytrip.core.domain.PlanRouteItem;
import com.ssafy.enjoytrip.core.domain.TravelPlan;
import com.ssafy.enjoytrip.core.domain.service.PlanService;
import com.ssafy.enjoytrip.core.api.web.dto.response.PlanResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.PlanRouteItemResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanResponseAssembler {
    private final PlanService planService;

    public PlanResponse toResponse(TravelPlan plan) {
        return new PlanResponse(
                plan.id(),
                plan.userId(),
                plan.title(),
                plan.startDate(),
                plan.endDate(),
                plan.budget(),
                value(plan.note()),
                normalizedItems(planService.findPlanItems(plan.id())),
                value(plan.createdAt())
        );
    }

    private static List<PlanRouteItemResponse> normalizedItems(List<PlanRouteItem> normalized) {
        if (normalized == null || normalized.isEmpty()) {
            return List.of();
        }
        return normalized.stream().map(PlanRouteItemResponse::from).toList();
    }

    private static String value(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}

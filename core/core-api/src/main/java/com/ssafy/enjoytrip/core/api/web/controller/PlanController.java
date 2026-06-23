package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId;
import com.ssafy.enjoytrip.core.api.web.api.PlanApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.PlanCreateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.PlanReplaceItemsRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.PlanUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.PlanResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.PlansResponse;
import com.ssafy.enjoytrip.core.domain.TravelPlan;
import com.ssafy.enjoytrip.core.domain.service.PlanService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Validated
public class PlanController implements PlanApi {
    private final PlanService service;

    @GetMapping
    @Override
    public ApiResponse<PlansResponse> find(@RequestParam(required = false) Long memberId) {
        List<PlanResponse> plans = memberId == null
                ? service.findAllPlans().stream().map(this::toResponse).toList()
                : service.findPlansByMemberId(memberId).stream().map(this::toResponse).toList();
        return success(new PlansResponse(plans));
    }

    @GetMapping("/{id}")
    @Override
    public ApiResponse<PlanResponse> findOne(
            @PathVariable @NotBlank String id
    ) {
        return success(toResponse(service.findRequiredPlan(id.strip())));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ApiResponse<Void> create(
            @Valid @RequestBody PlanCreateRequest request,
            @AuthenticatedMemberId Long memberId
    ) {
        service.createPlan(
                request.toTravelPlan(memberId),
                request.toPlanItems()
        );

        return success();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ApiResponse<Void> update(@PathVariable @NotBlank String id,
                                    @Valid @RequestBody PlanUpdateRequest request,
                                    @AuthenticatedMemberId Long memberId) {
        String planId = id.strip();
        service.updatePlan(
                memberId,
                planId,
                request.normalizedTitle(),
                request.normalizedStartDate(),
                request.normalizedEndDate(),
                request.budget(),
                request.normalizedNote(),
                request.toPlanItems(planId)
        );

        return success();
    }

    @PutMapping(value = "/{id}/items", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ApiResponse<Void> replaceItems(@PathVariable @NotBlank String id,
                                          @Valid @RequestBody PlanReplaceItemsRequest request,
                                          @AuthenticatedMemberId Long memberId) {
        String planId = id.strip();
        service.replacePlanItems(
                memberId,
                planId,
                request.toPlanItems(planId)
        );

        return success();
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Override
    public ApiResponse<Void> deleteItem(@PathVariable @NotBlank String id,
                                        @PathVariable @Positive Long itemId,
                                        @AuthenticatedMemberId Long memberId) {
        service.deletePlanItem(memberId, id.strip(), itemId);
        return success();
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(
            @PathVariable @NotBlank String id,
            @AuthenticatedMemberId Long memberId
    ) {
        service.deletePlan(memberId, id.strip());
        return success();
    }


    private PlanResponse toResponse(TravelPlan plan) {
        return PlanResponse.from(plan, service.findPlanItems(plan.id()));
    }

}

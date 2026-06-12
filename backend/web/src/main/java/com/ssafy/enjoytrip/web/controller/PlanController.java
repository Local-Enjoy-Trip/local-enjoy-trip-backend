package com.ssafy.enjoytrip.web.controller;

import static com.ssafy.enjoytrip.support.error.ErrorType.PLAN_NOT_FOUND;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.service.PlanService;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.api.PlanApi;
import com.ssafy.enjoytrip.web.dto.request.PlanCreateRequest;
import com.ssafy.enjoytrip.web.dto.request.PlanReplaceItemsRequest;
import com.ssafy.enjoytrip.web.dto.request.PlanUpdateRequest;
import com.ssafy.enjoytrip.web.dto.response.PlanResponse;
import com.ssafy.enjoytrip.web.dto.response.PlansResponse;
import com.ssafy.enjoytrip.web.mapper.PlanResponseAssembler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final PlanResponseAssembler responseAssembler;

    @GetMapping
    @Override
    public ApiResponse<PlansResponse> find(@RequestParam(required = false) String userId) {
        List<PlanResponse> plans = hasText(userId)
                ? service.findPlansByUser(userId.strip()).stream().map(responseAssembler::toResponse).toList()
                : service.findAllPlans().stream().map(responseAssembler::toResponse).toList();
        return success(new PlansResponse(plans));
    }

    @GetMapping("/{id}")
    @Override
    public ApiResponse<PlanResponse> findOne(
            @PathVariable @NotBlank String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return success(responseAssembler.toResponse(service.findPlan(id.strip())
                .orElseThrow(() -> new CoreException(PLAN_NOT_FOUND))));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ApiResponse<Void> create(
            @Valid @RequestBody PlanCreateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        service.createPlan(authenticatedUserId(jwt), request.toCommand());
        return success();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ApiResponse<Void> update(@PathVariable @NotBlank String id,
                                    @Valid @RequestBody PlanUpdateRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        service.updatePlan(authenticatedUserId(jwt), id.strip(), request.toCommand());
        return success();
    }

    @PutMapping(value = "/{id}/items", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ApiResponse<Void> replaceItems(@PathVariable @NotBlank String id,
                                          @Valid @RequestBody PlanReplaceItemsRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        service.replacePlanItems(authenticatedUserId(jwt), id.strip(), request.toCommands());
        return success();
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Override
    public ApiResponse<Void> deleteItem(@PathVariable @NotBlank String id,
                                        @PathVariable @Positive Long itemId,
                                        @AuthenticationPrincipal Jwt jwt) {
        service.deletePlanItem(authenticatedUserId(jwt), id.strip(), itemId);
        return success();
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable @NotBlank String id, @AuthenticationPrincipal Jwt jwt) {
        service.deletePlan(authenticatedUserId(jwt), id.strip());
        return success();
    }

    private static String authenticatedUserId(Jwt jwt) {
        return jwt.getSubject().strip();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

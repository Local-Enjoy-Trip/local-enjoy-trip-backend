package com.ssafy.enjoytrip.core.domain;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.PLAN_ACCESS_DENIED;

import com.ssafy.enjoytrip.core.domain.vo.DateRange;
import com.ssafy.enjoytrip.core.support.error.CoreException;

public record TravelPlan(
        String id,
        Long memberId,
        String title,
        DateRange planPeriod,
        int budget,
        String note,
        String routeItemsJson,
        String createdAt
) {
    private static final String DEFAULT_ROUTE_ITEMS_JSON = "[]";
    private static final String DEFAULT_CREATED_AT = "";

    public static TravelPlan createOwned(
            String id,
            Long memberId,
            String title,
            String startDate,
            String endDate,
            int budget,
            String note
    ) {
        return new TravelPlan(
                id,
                memberId,
                title,
                new DateRange(startDate, endDate),
                budget,
                note,
                DEFAULT_ROUTE_ITEMS_JSON,
                DEFAULT_CREATED_AT
        );
    }

    public TravelPlan merge(
            String title,
            String startDate,
            String endDate,
            Integer budget,
            String note
    ) {
        String newStartDate = valueOrCurrent(startDate, this.planPeriod.startDate());
        String newEndDate = valueOrCurrent(endDate, this.planPeriod.endDate());
        return new TravelPlan(
                id,
                memberId,
                valueOrCurrent(title, this.title),
                new DateRange(newStartDate, newEndDate),
                valueOrCurrent(budget, this.budget),
                valueOrCurrent(note, this.note),
                routeItemsJson,
                createdAt
        );
    }

    public void requireOwnedBy(Long memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new CoreException(PLAN_ACCESS_DENIED);
        }
    }

    private static <T> T valueOrCurrent(T value, T current) {
        if (value == null) {
            return current;
        }
        return value;
    }
}

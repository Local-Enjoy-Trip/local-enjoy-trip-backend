package com.ssafy.enjoytrip.core.domain;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.ACCESS_DENIED;

import com.ssafy.enjoytrip.core.support.error.CoreException;

public record TravelPlan(
        String id,
        String userId,
        String title,
        String startDate,
        String endDate,
        int budget,
        String note,
        String routeItemsJson,
        String createdAt
) {
    private static final String DEFAULT_ROUTE_ITEMS_JSON = "[]";
    private static final String DEFAULT_CREATED_AT = "";

    public static TravelPlan createOwned(
            String id,
            String userId,
            String title,
            String startDate,
            String endDate,
            int budget,
            String note
    ) {
        return new TravelPlan(
                id,
                userId,
                title,
                startDate,
                endDate,
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
        return new TravelPlan(
                id,
                userId,
                valueOrCurrent(title, this.title),
                valueOrCurrent(startDate, this.startDate),
                valueOrCurrent(endDate, this.endDate),
                valueOrCurrent(budget, this.budget),
                valueOrCurrent(note, this.note),
                routeItemsJson,
                createdAt
        );
    }

    public void requireOwnedBy(String userId) {
        if (!this.userId.equals(userId)) {
            throw new CoreException(ACCESS_DENIED);
        }
    }

    private static <T> T valueOrCurrent(T value, T current) {
        if (value == null) {
            return current;
        }
        return value;
    }
}

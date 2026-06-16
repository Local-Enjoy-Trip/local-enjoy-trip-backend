package com.ssafy.enjoytrip.domain;

import static com.ssafy.enjoytrip.support.error.ErrorType.ACCESS_DENIED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ssafy.enjoytrip.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TravelPlanTest {

    @DisplayName("새 계획 생성은 소유자와 기본 경로/생성시각 표현을 함께 만든다")
    @Test
    void createOwnedAppliesOwnerAndDefaults() {
        TravelPlan plan = TravelPlan.createOwned(
                "p1",
                "ssafy",
                "서울",
                "2026-05-14",
                "2026-05-15",
                1000,
                "note"
        );

        assertEquals("ssafy", plan.userId());
        assertEquals("[]", plan.routeItemsJson());
        assertEquals("", plan.createdAt());
    }

    @DisplayName("계획 병합은 null 입력만 기존 값으로 유지한다")
    @Test
    void mergeKeepsCurrentValuesOnlyForNullInputs() {
        TravelPlan current = plan();

        TravelPlan merged = current.merge(
                "부산",
                null,
                "2026-05-16",
                2000,
                ""
        );

        assertEquals("부산", merged.title());
        assertEquals("2026-05-14", merged.startDate());
        assertEquals("2026-05-16", merged.endDate());
        assertEquals(2000, merged.budget());
        assertEquals("", merged.note());
        assertEquals("[]", merged.routeItemsJson());
        assertEquals("created", merged.createdAt());
    }

    @DisplayName("계획 소유권 검증은 소유자만 허용하고 다른 사용자는 거부한다")
    @Test
    void requireOwnedByAllowsOnlyOwner() {
        TravelPlan plan = plan();

        assertDoesNotThrow(() -> plan.requireOwnedBy("owner"));

        CoreException exception = assertThrows(
                CoreException.class,
                () -> plan.requireOwnedBy("other")
        );

        assertEquals(ACCESS_DENIED, exception.errorType());
    }

    private static TravelPlan plan() {
        return new TravelPlan(
                "p1",
                "owner",
                "서울",
                "2026-05-14",
                "2026-05-15",
                1000,
                "note",
                "[]",
                "created"
        );
    }
}

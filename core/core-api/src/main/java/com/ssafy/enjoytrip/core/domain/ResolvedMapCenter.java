package com.ssafy.enjoytrip.core.domain;

public record ResolvedMapCenter(
        double longitude,
        double latitude,
        String regionName,
        boolean representativeLocationUsed
) {
}

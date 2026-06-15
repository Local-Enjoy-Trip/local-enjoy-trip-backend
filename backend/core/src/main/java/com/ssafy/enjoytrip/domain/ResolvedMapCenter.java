package com.ssafy.enjoytrip.domain;

public record ResolvedMapCenter(
        double longitude,
        double latitude,
        String regionName,
        boolean representativeLocationUsed
) {
}

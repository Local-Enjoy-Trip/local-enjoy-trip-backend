package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.AiCoursePreview;
import java.util.List;

public record AiCoursePreviewResponse(
        String title,
        String reason,
        List<StopPreview> stops,
        List<String> tags
) {
    public record StopPreview(
            long attractionId,
            String title,
            String addr1,
            String firstImage
    ) {}

    public static AiCoursePreviewResponse from(AiCoursePreview preview) {
        List<StopPreview> stops = preview.stops().stream()
                .map(s -> new StopPreview(s.attractionId(), s.title(), s.addr1(), s.firstImage()))
                .toList();
        return new AiCoursePreviewResponse(preview.title(), preview.reason(), stops, preview.tags());
    }
}

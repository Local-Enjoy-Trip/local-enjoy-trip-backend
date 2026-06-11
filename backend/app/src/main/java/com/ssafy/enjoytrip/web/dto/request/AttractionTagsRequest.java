package com.ssafy.enjoytrip.web.dto.request;

import jakarta.validation.constraints.Positive;
import java.util.List;

public record AttractionTagsRequest(
        List<@Positive Long> tagIds
) {
    public AttractionTagsRequest {
        if (tagIds == null) {
            tagIds = List.of();
        }
    }
}

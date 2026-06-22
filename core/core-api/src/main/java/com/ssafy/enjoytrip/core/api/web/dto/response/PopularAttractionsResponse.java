package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.api.web.dto.response.PopularAttraction;
import java.util.List;

public record PopularAttractionsResponse(List<PopularAttractionResponse> attractions) {
    public PopularAttractionsResponse {
        attractions = List.copyOf(attractions);
    }

    public static PopularAttractionsResponse from(List<PopularAttraction> attractions) {
        return new PopularAttractionsResponse(attractions.stream()
                .map(PopularAttractionResponse::new)
                .toList());
    }
}

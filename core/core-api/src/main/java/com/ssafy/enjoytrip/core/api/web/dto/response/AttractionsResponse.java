package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Attraction;
import java.util.List;

public record AttractionsResponse(List<Attraction> attractions) {
}

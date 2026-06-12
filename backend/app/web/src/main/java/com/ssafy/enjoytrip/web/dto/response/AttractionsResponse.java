package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.Attraction;
import java.util.List;

public record AttractionsResponse(List<Attraction> attractions) {
}

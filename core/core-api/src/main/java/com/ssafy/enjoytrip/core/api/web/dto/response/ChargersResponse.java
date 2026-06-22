package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.service.ChargerResult;
import java.util.List;

public record ChargersResponse(List<ChargerResult> chargers) {
    public ChargersResponse {
        chargers = List.copyOf(chargers);
    }
}

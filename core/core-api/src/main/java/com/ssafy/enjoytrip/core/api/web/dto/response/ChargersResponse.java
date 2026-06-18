package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.ChargerItem;
import java.util.List;

public record ChargersResponse(List<ChargerItem> chargers) {
}

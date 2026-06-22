package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.api.web.dto.response.ChargerItem;
import java.util.List;

public record ChargersResponse(List<ChargerItem> chargers) {
}

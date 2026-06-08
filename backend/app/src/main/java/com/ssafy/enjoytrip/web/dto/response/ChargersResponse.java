package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.ChargerItem;
import java.util.List;

public record ChargersResponse(List<ChargerItem> chargers) {
}

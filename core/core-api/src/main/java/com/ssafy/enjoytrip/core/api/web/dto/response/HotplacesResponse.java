package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Hotplace;
import java.util.List;

public record HotplacesResponse(List<Hotplace> hotplaces) {
}

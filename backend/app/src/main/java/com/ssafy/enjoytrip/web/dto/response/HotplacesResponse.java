package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.Hotplace;
import java.util.List;

public record HotplacesResponse(List<Hotplace> hotplaces) {
}

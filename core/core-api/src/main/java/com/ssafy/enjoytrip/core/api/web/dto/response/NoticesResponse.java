package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Notice;
import java.util.List;

public record NoticesResponse(List<Notice> notices) {
}

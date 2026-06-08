package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.Notice;
import java.util.List;

public record NoticesResponse(List<Notice> notices) {
}

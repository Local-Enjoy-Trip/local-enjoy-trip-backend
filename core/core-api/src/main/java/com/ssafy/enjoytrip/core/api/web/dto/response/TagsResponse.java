package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Tag;
import java.util.List;

public record TagsResponse(
        List<Tag> tags
) {
}

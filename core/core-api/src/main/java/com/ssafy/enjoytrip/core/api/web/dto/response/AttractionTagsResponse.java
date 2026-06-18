package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.AttractionTag;

import java.util.List;

public record AttractionTagsResponse(
        List<AttractionTag> tags
) {
}

package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.AttractionTag;

import java.util.List;

public record AttractionTagsResponse(
        List<AttractionTag> tags
) {
}

package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.BoardPost;
import java.util.List;

public record BoardsResponse(List<BoardPost> boards) {
}

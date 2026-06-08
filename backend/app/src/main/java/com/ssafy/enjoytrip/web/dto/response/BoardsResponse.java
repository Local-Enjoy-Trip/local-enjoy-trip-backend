package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.BoardPost;
import java.util.List;

public record BoardsResponse(List<BoardPost> boards) {
}

package com.ssafy.enjoytrip.web.dto.request;

import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_LATITUDE_OR_LONGITUDE;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_REQUEST;

import com.ssafy.enjoytrip.domain.MapExploreCommand;
import com.ssafy.enjoytrip.domain.MapExploreFilter;
import com.ssafy.enjoytrip.domain.NoteCategory;
import com.ssafy.enjoytrip.support.error.CoreException;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record MapExploreRequest(
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double mapX,
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double mapY,
        @Positive @Max(5000) Double radius,
        @Positive @Max(100) Integer limit,
        MapExploreFilter filter,
        NoteCategory noteCategory
) {
    public MapExploreCommand toCommand(String viewerUserId) {
        if ((mapX == null) != (mapY == null)) {
            throw new CoreException(INVALID_LATITUDE_OR_LONGITUDE);
        }
        if (radius != null && (radius <= 0 || radius > 5000)) {
            throw new CoreException(INVALID_REQUEST);
        }
        if (limit != null && (limit <= 0 || limit > 100)) {
            throw new CoreException(INVALID_REQUEST);
        }

        return new MapExploreCommand(
                viewerUserId,
                mapX,
                mapY,
                radius,
                limit,
                filter,
                noteCategory
        );
    }
}

package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseStop;
import com.ssafy.enjoytrip.core.domain.CourseTag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.IntStream;

public record CourseUpdateRequest(
        @NotBlank String title,
        String regionName,
        String date,
        List<@Valid CourseItemRequest> items,
        List<String> tags
) {
    public Course toCourse(String id, Long ownerMemberId) {
        return new Course(
                id,
                ownerMemberId,
                title.strip(),
                blankToNull(regionName),
                blankToNull(date),
                null,
                null,
                0,
                "",
                "",
                normalizedStops(),
                toCourseTags()
        );
    }

    private List<CourseStop> normalizedStops() {
        if (items == null) {
            return List.of();
        }
        return IntStream.range(0, items.size())
                .mapToObj(index -> items.get(index).toStop().withPosition(index + 1))
                .toList();
    }

    private List<CourseTag> toCourseTags() {
        if (tags == null) {
            return List.of();
        }
        return tags.stream()
                .map(String::strip)
                .filter(name -> !name.isBlank())
                .distinct()
                .map(name -> new CourseTag(null, name))
                .toList();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }
}

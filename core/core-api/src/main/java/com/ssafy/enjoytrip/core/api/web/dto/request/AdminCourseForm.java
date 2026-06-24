package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.Course;
import com.ssafy.enjoytrip.core.domain.CourseStop;
import com.ssafy.enjoytrip.core.domain.CourseStopTarget;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record AdminCourseForm(
        @NotBlank String id,
        @NotBlank String title,
        String regionName,
        String itemsText
) {
    public Course toCourse(Long adminMemberId) {
        return toCourse(adminMemberId, id.strip());
    }

    public Course toCourse(Long adminMemberId, String courseId) {
        return new Course(
                courseId,
                adminMemberId,
                title.strip(),
                blankToNull(regionName),
                null,
                true,
                null,
                null,
                0,
                "",
                "",
                stops(),
                List.of()
        );
    }

    private List<CourseStop> stops() {
        if (itemsText == null || itemsText.isBlank()) {
            return List.of();
        }

        List<CourseStop> result = new ArrayList<>();
        String[] lines = itemsText.strip().split("\\R");
        for (int index = 0; index < lines.length; index++) {
            result.add(toStop(lines[index], index + 1));
        }
        return result;
    }

    private static CourseStop toStop(String line, int position) {
        String[] parts = line.strip().split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("코스 아이템은 TYPE:ID 형식이어야 합니다.");
        }
        String type = parts[0].strip().toUpperCase(Locale.ROOT);
        Long id = Long.valueOf(parts[1].strip());
        return new CourseStop(null, target(type, id), position, null, null, null);
    }

    private static CourseStopTarget target(String type, Long id) {
        if ("ATTRACTION".equals(type)) {
            return CourseStopTarget.attraction(id);
        }
        if ("NOTE".equals(type)) {
            return CourseStopTarget.note(id);
        }
        throw new IllegalArgumentException("코스 아이템 type은 ATTRACTION 또는 NOTE만 허용됩니다.");
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }
}

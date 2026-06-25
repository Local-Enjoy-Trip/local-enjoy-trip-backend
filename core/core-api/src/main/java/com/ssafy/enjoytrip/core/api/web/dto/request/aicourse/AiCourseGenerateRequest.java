package com.ssafy.enjoytrip.core.api.web.dto.request.aicourse;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record AiCourseGenerateRequest(
        @Positive int sidoCode,
        int gugunCode,
        @NotNull Companion companion,
        @NotEmpty List<CourseTheme> themes,
        @NotNull CoursePace pace
) {}

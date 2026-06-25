package com.ssafy.enjoytrip.external.aicourse;

import java.util.List;

public record AiCourseGenerationInput(
        String neighborhood,
        String companionLabel,
        List<String> themeLabels,
        String paceLabel,
        int placeCount,
        List<AttractionItem> attractionCandidates,
        List<ReferenceCourse> referenceCourses,
        String userProfileDescription
) {
    public AiCourseGenerationInput {
        themeLabels = themeLabels == null ? List.of() : List.copyOf(themeLabels);
        attractionCandidates = attractionCandidates == null ? List.of() : List.copyOf(attractionCandidates);
        referenceCourses = referenceCourses == null ? List.of() : List.copyOf(referenceCourses);
    }

    public record AttractionItem(
            long id,
            String title,
            String addr1,
            String contentTypeId,
            String overview
    ) {}

    public record ReferenceCourse(
            String title,
            List<String> stopTitles
    ) {
        public ReferenceCourse {
            stopTitles = stopTitles == null ? List.of() : List.copyOf(stopTitles);
        }
    }
}

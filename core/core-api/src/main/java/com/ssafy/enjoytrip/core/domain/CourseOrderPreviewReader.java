package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.storage.db.core.model.AttractionRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseOrderPreviewReader {
    private final AttractionMapper attractionMapper;
    private final NoteMapper noteMapper;

    public CourseOrderPreview read(Course course) {
        Map<Long, AttractionRecord> attractions = findAttractions(course.route().stops());
        List<CourseOrderPreviewItem> items = course.route().stops().stream()
                .map(stop -> previewItem(stop, attractions))
                .toList();
        return new CourseOrderPreview(course, items);
    }

    private Map<Long, AttractionRecord> findAttractions(List<CourseStop> stops) {
        List<Long> attractionIds = stops.stream()
                .filter(stop -> stop.target() instanceof CourseStopTarget.Attraction)
                .map(stop -> stop.target().attractionIdOrNull())
                .distinct()
                .toList();
        if (attractionIds.isEmpty()) {
            return Map.of();
        }
        return attractionMapper.findByIds(attractionIds).stream()
                .collect(Collectors.toMap(AttractionRecord::id, Function.identity()));
    }

    private CourseOrderPreviewItem previewItem(CourseStop stop, Map<Long, AttractionRecord> attractions) {
        if (stop.target() instanceof CourseStopTarget.Attraction target) {
            AttractionRecord attraction = attractions.get(target.id());
            return new CourseOrderPreviewItem(
                    stop,
                    title(stop.title(), attraction == null ? null : attraction.title()),
                    attraction == null ? null : attraction.latitude(),
                    attraction == null ? null : attraction.longitude(),
                    attraction == null ? null : attraction.contentTypeId()
            );
        }
        if (stop.target() instanceof CourseStopTarget.Note target) {
            NoteRecord note = noteMapper.findById(target.id());
            return new CourseOrderPreviewItem(
                    stop,
                    title(stop.title(), note == null ? null : note.getTitle()),
                    note == null ? null : doubleValue(note.getLatitude()),
                    note == null ? null : doubleValue(note.getLongitude())
            );
        }
        return new CourseOrderPreviewItem(stop, stop.title(), null, null);
    }

    private static String title(String storedTitle, String resolvedTitle) {
        if (storedTitle != null && !storedTitle.isBlank()) {
            return storedTitle;
        }
        return resolvedTitle;
    }

    private static Double doubleValue(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}

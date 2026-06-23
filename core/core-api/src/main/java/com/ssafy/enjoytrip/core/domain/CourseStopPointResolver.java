package com.ssafy.enjoytrip.core.domain;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVALID_ITEM;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseStopPointResolver {
    private final AttractionMapper attractionMapper;
    private final NoteMapper noteMapper;

    public List<CourseStopPoint> resolveAll(List<CourseStop> stops) {
        return stops.stream()
                .map(this::resolve)
                .toList();
    }

    private CourseStopPoint resolve(CourseStop stop) {
        if (stop.target() instanceof CourseStopTarget.Attraction target) {
            return attractionPoint(stop, target.id());
        }
        if (stop.target() instanceof CourseStopTarget.Note target) {
            return notePoint(stop, target.id());
        }
        throw new CoreException(COURSE_INVALID_ITEM);
    }

    private CourseStopPoint attractionPoint(CourseStop stop, Long attractionId) {
        if (attractionMapper.existsPublicVisibleById(attractionId) <= 0) {
            throw new CoreException(COURSE_INVALID_ITEM);
        }

        AttractionRecord attraction = attractionMapper.findByIds(List.of(attractionId)).stream()
                .findFirst()
                .orElseThrow(() -> new CoreException(COURSE_INVALID_ITEM));

        return new CourseStopPoint(
                stop.withoutStorageId().withTitle(attraction.title()),
                attraction.title(),
                attraction.latitude(),
                attraction.longitude()
        );
    }

    private CourseStopPoint notePoint(CourseStop stop, Long noteId) {
        if (noteMapper.existsPublicActive(noteId) <= 0) {
            throw new CoreException(COURSE_INVALID_ITEM);
        }

        NoteRecord note = noteMapper.findById(noteId);
        if (note == null) {
            throw new CoreException(COURSE_INVALID_ITEM);
        }

        return new CourseStopPoint(
                stop.withoutStorageId().withTitle(note.getTitle()),
                note.getTitle(),
                doubleValue(note.getLatitude()),
                doubleValue(note.getLongitude())
        );
    }

    private static Double doubleValue(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}

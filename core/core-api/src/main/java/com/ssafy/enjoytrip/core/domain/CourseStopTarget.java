package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;

public sealed interface CourseStopTarget permits CourseStopTarget.Attraction, CourseStopTarget.Note {
    CourseStopTargetType type();

    Long id();

    default Long attractionIdOrNull() {
        if (this instanceof Attraction attraction) {
            return attraction.id();
        }
        return null;
    }

    default Long noteIdOrNull() {
        if (this instanceof Note note) {
            return note.id();
        }
        return null;
    }

    static CourseStopTarget attraction(Long attractionId) {
        return new Attraction(attractionId);
    }

    static CourseStopTarget note(Long noteId) {
        return new Note(noteId);
    }

    record Attraction(Long id) implements CourseStopTarget {
        public Attraction {
            requireId(id);
        }

        @Override
        public CourseStopTargetType type() {
            return CourseStopTargetType.ATTRACTION;
        }
    }

    record Note(Long id) implements CourseStopTarget {
        public Note {
            requireId(id);
        }

        @Override
        public CourseStopTargetType type() {
            return CourseStopTargetType.NOTE;
        }
    }

    private static void requireId(Long id) {
        if (id == null) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }
    }
}

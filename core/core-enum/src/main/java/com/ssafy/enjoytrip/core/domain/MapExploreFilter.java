package com.ssafy.enjoytrip.core.domain;

public enum MapExploreFilter {
    ALL,
    PLACE,
    NOTE,
    FRIEND,
    SAVED_PLACE;

    public boolean includesPlaces() {
        return this == ALL || this == PLACE || this == SAVED_PLACE;
    }

    public boolean includesNotes() {
        return this == ALL || this == NOTE || this == FRIEND;
    }

    public boolean friendNotesOnly() {
        return this == FRIEND;
    }

    public boolean savedPlacesOnly() {
        return this == SAVED_PLACE;
    }
}

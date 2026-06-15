package com.ssafy.enjoytrip.domain;

public enum MapExploreFilter {
    ALL,
    PLACE,
    NOTE,
    FRIEND;

    public boolean includesPlaces() {
        return this == ALL || this == PLACE;
    }

    public boolean includesNotes() {
        return this == ALL || this == NOTE || this == FRIEND;
    }

    public boolean friendNotesOnly() {
        return this == FRIEND;
    }
}

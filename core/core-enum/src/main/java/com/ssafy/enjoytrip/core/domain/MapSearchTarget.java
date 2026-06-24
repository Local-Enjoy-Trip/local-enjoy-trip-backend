package com.ssafy.enjoytrip.core.domain;

public enum MapSearchTarget {
    PLACE, NOTE, ALL;

    public boolean includesPlaces() {
        return this == PLACE || this == ALL;
    }

    public boolean includesNotes() {
        return this == NOTE || this == ALL;
    }
}

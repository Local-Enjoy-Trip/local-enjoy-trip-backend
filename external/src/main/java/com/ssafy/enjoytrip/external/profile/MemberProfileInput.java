package com.ssafy.enjoytrip.external.profile;

import java.util.List;

public record MemberProfileInput(
        List<SavedAttractionItem> attractions,
        List<SavedNoteItem> notes
) {
    public record SavedAttractionItem(String title, String addr1, String contentTypeId) {}

    public record SavedNoteItem(String title, String category, String tagNames) {}
}

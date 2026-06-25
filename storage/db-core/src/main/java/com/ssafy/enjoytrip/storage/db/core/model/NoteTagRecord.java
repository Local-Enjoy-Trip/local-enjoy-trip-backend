package com.ssafy.enjoytrip.storage.db.core.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoteTagRecord {
    private Long id;
    private Long noteId;
    private Long tagId;
    private String tagName;
}

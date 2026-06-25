package com.ssafy.enjoytrip.storage.db.core.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedAttractionInputRecord {
    private String title;
    private String addr1;
    private String addr2;
    private String contentTypeId;
}

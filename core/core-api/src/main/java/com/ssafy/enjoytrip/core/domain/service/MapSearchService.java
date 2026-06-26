package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.MapPin;
import com.ssafy.enjoytrip.core.domain.MapSearchTarget;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.NoteMapPin;
import com.ssafy.enjoytrip.core.domain.NoteVisibility;
import com.ssafy.enjoytrip.core.domain.NoteViewerRelationship;
import com.ssafy.enjoytrip.core.domain.PlaceMapPin;
import com.ssafy.enjoytrip.external.minio.MinioNoteImageUploadUrlGenerator;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteMapPinRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapSearchService {
    private final NoteMapper noteMapper;
    private final AttractionMapper attractionMapper;
    private final MinioNoteImageUploadUrlGenerator noteImageUploadUrlGenerator;

    public List<MapPin> search(
            String keyword,
            double longitude,
            double latitude,
            Double radius,
            MapSearchTarget target,
            NoteCategory noteCategory,
            int limit,
            Long viewerMemberId
    ) {
        String escapedKeyword = escapeIlikeWildcards(keyword);

        List<MapPin> merged = new ArrayList<>();

        if (target.includesPlaces()) {
            List<AttractionSearchRecord> records = fetchPlaces(keyword, escapedKeyword, longitude, latitude, radius, limit, viewerMemberId);
            merged.addAll(toPlacePins(records, keyword));
        }

        if (target.includesNotes()) {
            List<NoteMapPinRecord> records = fetchNotes(keyword, escapedKeyword, longitude, latitude, radius, noteCategory, limit, viewerMemberId);
            merged.addAll(toNotePins(records, keyword));
        }

        sortPins(merged);

        return merged;
    }

    private List<AttractionSearchRecord> fetchPlaces(
            String keyword,
            String escapedKeyword,
            double longitude,
            double latitude,
            Double radius,
            int limit,
            Long viewerMemberId
    ) {
        return attractionMapper.searchMapPlaces(
                keyword,
                escapedKeyword,
                longitude,
                latitude,
                radius,
                limit,
                viewerMemberId
        );
    }

    private List<MapPin> toPlacePins(List<AttractionSearchRecord> records, String keyword) {
        List<MapPin> pins = new ArrayList<>();
        for (AttractionSearchRecord r : records) {
            int matchTier = r.title().equalsIgnoreCase(keyword) ? 0 : 1;
            pins.add(new PlaceMapPin(
                    r.id(),
                    r.title(),
                    r.addr1(),
                    r.latitude(),
                    r.longitude(),
                    r.firstImage(),
                    r.contentTypeId(),
                    r.distanceMeters(),
                    r.saved(),
                    r.saveCount(),
                    r.ratingAverage(),
                    r.ratingCount(),
                    matchTier
            ));
        }
        return pins;
    }

    private List<NoteMapPinRecord> fetchNotes(
            String keyword,
            String escapedKeyword,
            double longitude,
            double latitude,
            Double radius,
            NoteCategory noteCategory,
            int limit,
            Long viewerMemberId
    ) {
        String categoryStr = noteCategory == null ? null : noteCategory.name();
        return noteMapper.searchMapNotes(
                keyword,
                escapedKeyword,
                longitude,
                latitude,
                radius,
                categoryStr,
                limit,
                viewerMemberId
        );
    }

    private List<MapPin> toNotePins(List<NoteMapPinRecord> records, String keyword) {
        List<MapPin> pins = new ArrayList<>();
        for (NoteMapPinRecord r : records) {
            int matchTier = r.title().equalsIgnoreCase(keyword) ? 0 : 1;
            String objectKey = r.imageObjectKey();
            String imageUrl = objectKey != null ? noteImageUploadUrlGenerator.publicUrl(objectKey) : null;
            pins.add(new NoteMapPin(
                    r.id(),
                    r.title(),
                    NoteCategory.valueOf(r.category()),
                    NoteVisibility.valueOf(r.visibility()),
                    r.latitude().doubleValue(),
                    r.longitude().doubleValue(),
                    r.regionName(),
                    r.distanceMeters(),
                    objectKey,
                    imageUrl,
                    r.authorNickname(),
                    r.authorProfileImageUrl(),
                    NoteViewerRelationship.valueOf(r.relationship()),
                    r.createdAt(),
                    matchTier
            ));
        }
        return pins;
    }

    private void sortPins(List<MapPin> pins) {
        pins.sort(Comparator.comparingInt(MapPin::matchTier)
                .thenComparingDouble(MapPin::distanceMeters));
    }

    private String escapeIlikeWildcards(String keyword) {
        if (keyword == null) {
            return null;
        }
        return keyword.replace("\\", "\\\\")
                      .replace("%", "\\%")
                      .replace("_", "\\_");
    }
}

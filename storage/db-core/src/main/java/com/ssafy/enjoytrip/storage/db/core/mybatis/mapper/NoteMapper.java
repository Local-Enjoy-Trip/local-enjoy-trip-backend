package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.NoteMapPinRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NoteMapper {
    NoteRecord insert(NoteRecord record);

    NoteRecord findById(Long id);

    NoteRecord updateOwned(NoteRecord record);

    int softDeleteOwned(@Param("id") Long id, @Param("authorMemberId") Long authorMemberId);

    int existsAccessibleActive(@Param("noteId") Long noteId, @Param("viewerMemberId") Long viewerMemberId);

    int existsPublicActive(Long noteId);

    int insertSave(@Param("noteId") Long noteId, @Param("memberId") Long memberId);

    int deleteSave(@Param("noteId") Long noteId, @Param("memberId") Long memberId);

    List<NoteRecord> findSavedAccessible(@Param("viewerMemberId") Long memberId, @Param("limit") int limit);

    List<NoteRecord> findWritten(@Param("authorMemberId") Long memberId, @Param("limit") int limit);

    List<NoteRecord> findNearbyAccessible(@Param("longitude") double longitude,
                                          @Param("latitude") double latitude,
                                          @Param("radiusMeters") double radiusMeters,
                                          @Param("limit") int limit,
                                          @Param("viewerMemberId") Long viewerMemberId);

    List<NoteMapPinRecord> findMapPins(@Param("longitude") double longitude,
                                        @Param("latitude") double latitude,
                                        @Param("radiusMeters") double radiusMeters,
                                        @Param("limit") Integer limit,
                                        @Param("viewerMemberId") Long viewerMemberId,
                                        @Param("category") String category,
                                        @Param("friendOnly") boolean friendOnly);

    List<NoteMapPinRecord> searchMapNotes(@Param("keyword") String keyword,
                                          @Param("escapedKeyword") String escapedKeyword,
                                          @Param("longitude") double longitude,
                                          @Param("latitude") double latitude,
                                          @Param("radiusMeters") Double radiusMeters,
                                          @Param("category") String category,
                                          @Param("limit") Integer limit,
                                          @Param("viewerMemberId") Long viewerMemberId);
}

package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.NoteMapPinRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NoteMapper {
    NoteRecord insert(NoteRecord record);

    NoteRecord findById(Long id);

    NoteRecord updateOwned(NoteRecord record);

    int softDeleteOwned(@Param("id") Long id, @Param("authorUserId") String authorUserId);

    int existsAccessibleActive(@Param("noteId") Long noteId, @Param("viewerUserId") String viewerUserId);

    int insertSave(@Param("noteId") Long noteId, @Param("userId") String userId);

    int deleteSave(@Param("noteId") Long noteId, @Param("userId") String userId);

    List<NoteRecord> findSavedAccessible(@Param("viewerUserId") String userId, @Param("limit") int limit);

    List<NoteRecord> findNearbyAccessible(@Param("longitude") double longitude,
                                          @Param("latitude") double latitude,
                                          @Param("radiusMeters") double radiusMeters,
                                          @Param("limit") int limit,
                                          @Param("viewerUserId") String viewerUserId);

    List<NoteMapPinRecord> findMapPins(@Param("longitude") double longitude,
                                        @Param("latitude") double latitude,
                                        @Param("radiusMeters") double radiusMeters,
                                        @Param("limit") Integer limit,
                                        @Param("viewerUserId") String viewerUserId,
                                        @Param("category") String category,
                                        @Param("friendOnly") boolean friendOnly);
}

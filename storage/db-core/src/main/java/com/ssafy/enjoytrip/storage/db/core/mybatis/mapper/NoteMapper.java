package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteMapPinRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NoteMapper {
    NoteRecord insert(NoteRecord record);

    NoteRecord findById(Long id);

    NoteRecord updateOwned(NoteRecord record);

    int softDeleteOwned(@Param("id") Long id, @Param("authorUserId") String authorUserId);

    List<NoteRecord> findNearbyAccessible(@Param("longitude") double longitude,
                                          @Param("latitude") double latitude,
                                          @Param("radiusMeters") double radiusMeters,
                                          @Param("limit") int limit,
                                          @Param("viewerUserId") String viewerUserId);

    List<NoteMapPinRecord> findMapPins(@Param("longitude") double longitude,
                                    @Param("latitude") double latitude,
                                    @Param("radiusMeters") double radiusMeters,
                                    @Param("limit") int limit,
                                    @Param("viewerUserId") String viewerUserId,
                                    @Param("category") String category,
                                    @Param("friendOnly") boolean friendOnly);
}

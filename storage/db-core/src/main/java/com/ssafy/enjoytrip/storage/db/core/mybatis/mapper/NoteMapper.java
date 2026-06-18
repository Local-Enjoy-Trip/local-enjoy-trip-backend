package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.entity.NoteEntity;
import com.ssafy.enjoytrip.storage.db.core.mybatis.row.NoteMapPinRow;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NoteMapper {
    NoteEntity insert(NoteEntity entity);

    NoteEntity findById(Long id);

    NoteEntity updateOwned(NoteEntity entity);

    int softDeleteOwned(@Param("id") Long id, @Param("authorUserId") String authorUserId);

    List<NoteEntity> findNearbyAccessible(@Param("longitude") double longitude,
                                          @Param("latitude") double latitude,
                                          @Param("radiusMeters") double radiusMeters,
                                          @Param("limit") int limit,
                                          @Param("viewerUserId") String viewerUserId);

    List<NoteMapPinRow> findMapPins(@Param("longitude") double longitude,
                                    @Param("latitude") double latitude,
                                    @Param("radiusMeters") double radiusMeters,
                                    @Param("limit") int limit,
                                    @Param("viewerUserId") String viewerUserId,
                                    @Param("category") String category,
                                    @Param("friendOnly") boolean friendOnly);
}

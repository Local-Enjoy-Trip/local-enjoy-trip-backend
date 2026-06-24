package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import org.apache.ibatis.annotations.Param;

public interface NoteEmbeddingMapper {
    int upsertEmbedded(@Param("noteId") Long noteId,
                       @Param("vectorLiteral") String vectorLiteral,
                       @Param("sourceVersion") String sourceVersion,
                       @Param("sourceTextHash") String sourceTextHash,
                       @Param("dimension") int dimension,
                       @Param("embeddingInput") String embeddingInput,
                       @Param("provider") String provider,
                       @Param("model") String model);

    int upsertFailed(@Param("noteId") Long noteId,
                     @Param("sourceVersion") String sourceVersion,
                     @Param("sourceTextHash") String sourceTextHash,
                     @Param("provider") String provider,
                     @Param("model") String model,
                     @Param("failureCode") String failureCode,
                     @Param("failureMessage") String failureMessage);
}

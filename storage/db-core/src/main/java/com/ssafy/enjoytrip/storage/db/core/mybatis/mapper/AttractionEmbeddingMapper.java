package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.mybatis.row.AttractionEmbeddingSourceRow;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AttractionEmbeddingMapper {
    List<AttractionEmbeddingSourceRow> findTargets(@Param("regions") List<TargetRegionRow> regions,
                                                   @Param("limit") int limit);

    int existsEmbeddedWithSameSource(@Param("attractionId") Long attractionId,
                                     @Param("sourceVersion") String sourceVersion,
                                     @Param("sourceTextHash") String sourceTextHash);

    int upsertEmbedded(@Param("attractionId") Long attractionId,
                       @Param("vectorLiteral") String vectorLiteral,
                       @Param("sourceVersion") String sourceVersion,
                       @Param("sourceTextHash") String sourceTextHash,
                       @Param("dimension") int dimension,
                       @Param("embeddingInput") String embeddingInput,
                       @Param("provider") String provider,
                       @Param("model") String model);

    int upsertFailed(@Param("attractionId") Long attractionId,
                     @Param("sourceVersion") String sourceVersion,
                     @Param("sourceTextHash") String sourceTextHash,
                     @Param("failureCode") String failureCode,
                     @Param("failureMessage") String failureMessage);

    long countOutsideTargetRegions(@Param("regions") List<TargetRegionRow> regions);

    record TargetRegionRow(Integer sidoCode, Integer gugunCode) {
    }
}

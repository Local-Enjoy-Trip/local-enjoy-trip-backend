package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.CourseEmbeddingInputRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CourseEmbeddingMapper {

    CourseEmbeddingInputRecord findCourseEmbeddingInputById(@Param("courseId") String courseId);

    int markPending(@Param("courseId") String courseId);

    List<String> claimPendingBatch(@Param("limit") int limit);

    int upsertEmbedded(
            @Param("courseId") String courseId,
            @Param("description") String description,
            @Param("vectorLiteral") String vectorLiteral,
            @Param("dominantCategory") String dominantCategory,
            @Param("sourceVersion") String sourceVersion,
            @Param("sourceHash") String sourceHash,
            @Param("dimension") int dimension,
            @Param("provider") String provider,
            @Param("model") String model
    );

    int upsertFailed(
            @Param("courseId") String courseId,
            @Param("sourceVersion") String sourceVersion,
            @Param("sourceHash") String sourceHash,
            @Param("provider") String provider,
            @Param("model") String model,
            @Param("failureCode") String failureCode,
            @Param("failureMessage") String failureMessage
    );

    String findSourceHashByCourseId(@Param("courseId") String courseId);
}

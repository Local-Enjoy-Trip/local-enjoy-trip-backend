package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.SavedAttractionInputRecord;
import com.ssafy.enjoytrip.storage.db.core.model.SavedNoteInputRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MemberProfileEmbeddingMapper {

    List<SavedAttractionInputRecord> findSavedAttractionInputsByMemberId(
            @Param("memberId") Long memberId
    );

    List<SavedNoteInputRecord> findSavedNoteInputsByMemberId(
            @Param("memberId") Long memberId
    );

    int upsertEmbedded(
            @Param("memberId") Long memberId,
            @Param("profileDescription") String profileDescription,
            @Param("vectorLiteral") String vectorLiteral,
            @Param("sourceVersion") String sourceVersion,
            @Param("sourceHash") String sourceHash,
            @Param("dimension") int dimension,
            @Param("provider") String provider,
            @Param("model") String model
    );

    int upsertFailed(
            @Param("memberId") Long memberId,
            @Param("sourceVersion") String sourceVersion,
            @Param("sourceHash") String sourceHash,
            @Param("provider") String provider,
            @Param("model") String model,
            @Param("failureCode") String failureCode,
            @Param("failureMessage") String failureMessage
    );

    String findSourceHashByMemberId(@Param("memberId") Long memberId);

    String findProfileDescriptionByMemberId(@Param("memberId") Long memberId);

    boolean hasMemberProfileEmbedding(@Param("memberId") Long memberId);
}

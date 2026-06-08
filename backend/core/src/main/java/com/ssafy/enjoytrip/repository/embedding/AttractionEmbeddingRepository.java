package com.ssafy.enjoytrip.repository.embedding;

import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingFailure;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingResult;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingSource;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingTargetRegion;

import java.util.List;

public interface AttractionEmbeddingRepository {
    List<AttractionEmbeddingSource> findTargets(
            List<AttractionEmbeddingTargetRegion> targetRegions,
            int limit
    );

    boolean isEmbeddedWithSameSource(Long attractionId, String sourceVersion, String sourceTextHash);

    void saveEmbedded(
            AttractionEmbeddingSource source,
            String sourceVersion,
            String sourceTextHash,
            String embeddingInput,
            AttractionEmbeddingResult result
    );

    void saveFailed(AttractionEmbeddingSource source, String sourceVersion, String sourceTextHash,
                    AttractionEmbeddingFailure failure);

    long countEmbeddingsOutsideTargetRegions(List<AttractionEmbeddingTargetRegion> targetRegions);
}

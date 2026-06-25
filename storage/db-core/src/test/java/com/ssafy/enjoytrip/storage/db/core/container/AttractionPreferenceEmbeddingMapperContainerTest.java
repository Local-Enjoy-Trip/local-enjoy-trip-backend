package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.AttractionEmbeddingCandidateRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Tag("pgvector")
class AttractionPreferenceEmbeddingMapperContainerTest extends StorageContainerTestSupport {

    @Autowired
    private AttractionMapper attractionMapper;

    @DisplayName("findCandidatesByPreferenceEmbedding은 EMBEDDED 상태의 임베딩이 있는 관광지만 반환한다")
    @Test
    void findCandidatesByPreferenceEmbeddingReturnsOnlyEmbeddedAttractions() {
        long attractionId = 8800001L;
        long noEmbedId = 8800002L;
        seedAttraction(attractionId, "임베딩 관광지", 1, 1);
        seedAttraction(noEmbedId, "임베딩 없는 관광지", 1, 1);
        seedAttractionEmbedding(attractionId, vectorLiteral(1536), "EMBEDDED");

        List<AttractionEmbeddingCandidateRecord> results =
                attractionMapper.findCandidatesByPreferenceEmbedding(
                        1, null, vectorLiteral(1536), 20
                );

        List<Long> ids = results.stream()
                .map(AttractionEmbeddingCandidateRecord::getId)
                .toList();
        assertThat(ids).contains(attractionId);
        assertThat(ids).doesNotContain(noEmbedId);
    }

    @DisplayName("findCandidatesByPreferenceEmbedding은 sidoCode로 필터링한다")
    @Test
    void findCandidatesByPreferenceEmbeddingFiltersBySidoCode() {
        long seoulAttractionId = 8800101L;
        long busanAttractionId = 8800102L;
        seedAttraction(seoulAttractionId, "서울 관광지", 1, 1);
        seedAttraction(busanAttractionId, "부산 관광지", 6, 1);
        seedAttractionEmbedding(seoulAttractionId, vectorLiteral(1536), "EMBEDDED");
        seedAttractionEmbedding(busanAttractionId, vectorLiteral(1536), "EMBEDDED");

        List<AttractionEmbeddingCandidateRecord> results =
                attractionMapper.findCandidatesByPreferenceEmbedding(
                        1, null, vectorLiteral(1536), 20
                );

        List<Long> ids = results.stream()
                .map(AttractionEmbeddingCandidateRecord::getId)
                .toList();
        assertThat(ids).contains(seoulAttractionId);
        assertThat(ids).doesNotContain(busanAttractionId);
    }

    @DisplayName("findCandidatesByPreferenceEmbedding은 gugunCode가 0이면 전체 구군 관광지를 반환한다")
    @Test
    void findCandidatesByPreferenceEmbeddingReturnsAllGugunWhenGugunCodeIsZero() {
        long attraction1 = 8800201L;
        long attraction2 = 8800202L;
        seedAttraction(attraction1, "중구 관광지", 1, 11);
        seedAttraction(attraction2, "강남 관광지", 1, 25);
        seedAttractionEmbedding(attraction1, vectorLiteral(1536), "EMBEDDED");
        seedAttractionEmbedding(attraction2, vectorLiteral(1536), "EMBEDDED");

        List<AttractionEmbeddingCandidateRecord> results =
                attractionMapper.findCandidatesByPreferenceEmbedding(
                        1, 0, vectorLiteral(1536), 20
                );

        List<Long> ids = results.stream()
                .map(AttractionEmbeddingCandidateRecord::getId)
                .toList();
        assertThat(ids).contains(attraction1, attraction2);
    }

    @DisplayName("findCandidatesByPreferenceEmbedding은 limit 개수만큼만 반환한다")
    @Test
    void findCandidatesByPreferenceEmbeddingRespectsLimit() {
        for (long i = 0; i < 5; i++) {
            long id = 8800301L + i;
            seedAttraction(id, "관광지" + i, 1, 1);
            seedAttractionEmbedding(id, vectorLiteral(1536), "EMBEDDED");
        }

        List<AttractionEmbeddingCandidateRecord> results =
                attractionMapper.findCandidatesByPreferenceEmbedding(
                        1, null, vectorLiteral(1536), 3
                );

        assertThat(results).hasSize(3);
    }

    @DisplayName("findCandidatesByPreferenceEmbedding 결과는 id, title, addr1, contentTypeId, overview 필드를 반환한다")
    @Test
    void findCandidatesByPreferenceEmbeddingReturnsRequiredFields() {
        long attractionId = 8800401L;
        seedAttraction(attractionId, "경복궁", 1, 1);
        seedAttractionEmbedding(attractionId, vectorLiteral(1536), "EMBEDDED");

        List<AttractionEmbeddingCandidateRecord> results =
                attractionMapper.findCandidatesByPreferenceEmbedding(
                        1, null, vectorLiteral(1536), 20
                );

        AttractionEmbeddingCandidateRecord record = results.stream()
                .filter(r -> attractionId == r.getId())
                .findFirst()
                .orElseThrow();
        assertThat(record.getTitle()).isEqualTo("경복궁");
        assertThat(record.getAddr1()).isNotNull();
        assertThat(record.getContentTypeId()).isNotNull();
    }

    private void seedAttractionEmbedding(long attractionId, String vectorLiteral, String status) {
        jdbcTemplate.update("""
                insert into attraction_embeddings
                    (attraction_id, embedding, source_version, source_text_hash,
                     embedding_dimension, embedding_input, provider, model, status)
                values (?, ?::vector, 'v1', 'hash', 1536, '설명', 'openai', 'text-embedding-3-small', ?)
                on conflict (attraction_id) do update
                    set embedding = excluded.embedding, status = excluded.status
                """, attractionId, vectorLiteral, status);
    }

    private static String vectorLiteral(int dimension) {
        return "[" + "0.001,".repeat(dimension - 1) + "0.001]";
    }
}

package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberProfileEmbeddingMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AttractionRecommendationMapperH2Test extends H2MapperTestSupport {

    @Autowired
    private MemberProfileEmbeddingMapper memberProfileEmbeddingMapper;

    @DisplayName("hasMemberProfileEmbedding은 임베딩이 없는 회원이면 false를 반환한다")
    @Test
    void hasMemberProfileEmbeddingReturnsFalseWhenNoEmbedding() {
        Long memberId = seedMember("tester", "tester@example.com");

        boolean result = memberProfileEmbeddingMapper.hasMemberProfileEmbedding(memberId);

        assertThat(result).isFalse();
    }

    @DisplayName("hasMemberProfileEmbedding은 FAILED 상태 임베딩이면 false를 반환한다")
    @Test
    void hasMemberProfileEmbeddingReturnsFalseWhenStatusFailed() {
        Long memberId = seedMember("tester2", "tester2@example.com");
        jdbcTemplate.update("""
                insert into member_profile_embeddings (
                    member_id, source_version, source_hash, embedding_dimension,
                    provider, model, status, attempt_count
                )
                values (?, ?, ?, ?, ?, ?, 'FAILED', 1)
                """,
                memberId, "v1", "abc123", 1536, "openai", "text-embedding-3-small");

        boolean result = memberProfileEmbeddingMapper.hasMemberProfileEmbedding(memberId);

        assertThat(result).isFalse();
    }
}

package com.ssafy.enjoytrip.core.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.AiCoursePreview;
import com.ssafy.enjoytrip.core.domain.CourseReader;
import com.ssafy.enjoytrip.external.aicourse.AiCourseGenerationClient;
import com.ssafy.enjoytrip.external.aicourse.AiCourseGenerationInput;
import com.ssafy.enjoytrip.external.aicourse.AiCourseGenerationResult;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionEmbeddingCandidateRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberProfileEmbeddingMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class AiCourseGenerationServiceTest {

    @Mock
    private AttractionMapper attractionMapper;

    @Mock
    private CourseReader courseReader;

    @Mock
    private MemberProfileEmbeddingMapper memberProfileEmbeddingMapper;

    @Mock
    private ObjectProvider<EmbeddingModel> embeddingModelProvider;

    @Mock
    private AiCourseGenerationClient aiCourseGenerationClient;

    @Mock
    private EmbeddingModel embeddingModel;

    private AiCourseGenerationService service;

    @BeforeEach
    void setUp() {
        service = new AiCourseGenerationService(
                attractionMapper,
                courseReader,
                memberProfileEmbeddingMapper,
                embeddingModelProvider,
                aiCourseGenerationClient
        );
    }

    @DisplayName("generatePreview는 취향 임베딩 기반으로 관광지 후보를 조회하고 AI 코스를 생성한다")
    @Test
    void generatePreviewCallsEmbeddingAndAiClient() {
        long attractionId = 101L;
        stubEmbeddingModel(1536);
        when(attractionMapper.findCandidatesByPreferenceEmbedding(
                isNull(), isNull(), isNull(), anyString(), eq(1)
        )).thenReturn(List.of(candidateRecord(attractionId, "카페 어니언")));
        when(attractionMapper.findCandidatesWithinRadius(
                isNull(), anyDouble(), anyDouble(), anyDouble(), anyString(), eq(attractionId), eq(20)
        )).thenReturn(List.of());
        when(courseReader.hasMemberProfileEmbedding(1L)).thenReturn(false);
        when(memberProfileEmbeddingMapper.findProfileDescriptionByMemberId(1L)).thenReturn(null);
        when(aiCourseGenerationClient.generate(any())).thenReturn(
                new AiCourseGenerationResult("감성 카페 투어", List.of(attractionId), "좋은 카페들", List.of("감성카페"))
        );
        when(attractionMapper.findByIds(List.of(attractionId))).thenReturn(
                List.of(attractionRecord(attractionId, "카페 어니언", "망원동 카페거리", "http://img.jpg"))
        );

        AiCoursePreview preview = service.generatePreview(
                1L, null, "연인과", List.of("감성 카페"), "여유롭게", 3
        );

        assertThat(preview.title()).isEqualTo("감성 카페 투어");
        assertThat(preview.reason()).isEqualTo("좋은 카페들");
        assertThat(preview.stops()).hasSize(1);
        assertThat(preview.stops().get(0).attractionId()).isEqualTo(attractionId);
        assertThat(preview.stops().get(0).title()).isEqualTo("카페 어니언");
        assertThat(preview.stops().get(0).firstImage()).isEqualTo("http://img.jpg");
    }

    @DisplayName("generatePreview는 regionName이 제공되면 1차 검색에서 시도와 regionName 조건으로 전달한다")
    @Test
    void generatePreviewPassesNullGugunFilterWhenRegionNameIsProvided() {
        stubEmbeddingModel(1536);
        when(attractionMapper.findCandidatesByPreferenceEmbedding(
                isNull(), isNull(), eq("망원동"), anyString(), eq(1)
        )).thenReturn(List.of(candidateRecord(101L, "테스트")));
        when(attractionMapper.findCandidatesWithinRadius(
                isNull(), anyDouble(), anyDouble(), anyDouble(), anyString(), anyLong(), eq(20)
        )).thenReturn(List.of());
        when(courseReader.hasMemberProfileEmbedding(1L)).thenReturn(false);
        when(memberProfileEmbeddingMapper.findProfileDescriptionByMemberId(1L)).thenReturn(null);
        when(aiCourseGenerationClient.generate(any())).thenReturn(
                new AiCourseGenerationResult("코스", List.of(), "이유", List.of())
        );

        service.generatePreview(1L, "망원동", "친구와", List.of("맛집"), "알차게", 5);

        verify(attractionMapper).findCandidatesByPreferenceEmbedding(
                isNull(), isNull(), eq("망원동"), anyString(), eq(1)
        );
    }


    @DisplayName("generatePreview는 멤버 프로필 임베딩이 없으면 참조 코스를 조회하지 않는다")
    @Test
    void generatePreviewSkipsReferenceCourseWhenNoMemberProfileEmbedding() {
        stubEmbeddingModel(1536);
        when(attractionMapper.findCandidatesByPreferenceEmbedding(
                any(), any(), any(), anyString(), eq(1)
        )).thenReturn(List.of(candidateRecord(101L, "테스트")));
        when(attractionMapper.findCandidatesWithinRadius(
                any(), anyDouble(), anyDouble(), anyDouble(), anyString(), anyLong(), eq(20)
        )).thenReturn(List.of());
        when(courseReader.hasMemberProfileEmbedding(1L)).thenReturn(false);
        when(memberProfileEmbeddingMapper.findProfileDescriptionByMemberId(1L)).thenReturn(null);
        when(aiCourseGenerationClient.generate(any())).thenReturn(
                new AiCourseGenerationResult("코스", List.of(), "이유", List.of())
        );

        service.generatePreview(1L, null, "혼자", List.of("산책"), "알맞게", 4);

        verify(courseReader, never()).findCandidatesByMemberProfile(any(), anyInt());
    }

    private void stubEmbeddingModel(int dimension) {
        when(embeddingModelProvider.getIfAvailable()).thenReturn(embeddingModel);
        float[] vector = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            vector[i] = 0.001f;
        }
        org.springframework.ai.embedding.Embedding embedding =
                new org.springframework.ai.embedding.Embedding(vector, 0);
        EmbeddingResponse response = new EmbeddingResponse(List.of(embedding));
        when(embeddingModel.call(any())).thenReturn(response);
    }

    private static AttractionEmbeddingCandidateRecord candidateRecord(long id, String title) {
        try {
            java.lang.reflect.Constructor<AttractionEmbeddingCandidateRecord> ctor =
                    AttractionEmbeddingCandidateRecord.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            AttractionEmbeddingCandidateRecord record = ctor.newInstance();
            org.springframework.test.util.ReflectionTestUtils.setField(record, "id", id);
            org.springframework.test.util.ReflectionTestUtils.setField(record, "title", title);
            org.springframework.test.util.ReflectionTestUtils.setField(record, "addr1", "망원동");
            org.springframework.test.util.ReflectionTestUtils.setField(record, "addr2", "역삼동");
            org.springframework.test.util.ReflectionTestUtils.setField(record, "latitude", 37.5);
            org.springframework.test.util.ReflectionTestUtils.setField(record, "longitude", 126.9);
            return record;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static AttractionRecord attractionRecord(long id, String title, String addr1, String firstImage) {
        return new AttractionRecord(id, title, addr1, "역삼동", null, null, firstImage,
                null, 0, 1, 1, 37.5, 126.9, null, "12", null);
    }
}

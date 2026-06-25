package com.ssafy.enjoytrip.core.domain.service;

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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCourseGenerationService {
    private static final int ATTRACTION_CANDIDATE_LIMIT = 20;
    private static final int REFERENCE_COURSE_LIMIT = 5;

    private final AttractionMapper attractionMapper;
    private final CourseReader courseReader;
    private final MemberProfileEmbeddingMapper memberProfileEmbeddingMapper;
    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;
    private final AiCourseGenerationClient aiCourseGenerationClient;

    public AiCoursePreview generatePreview(
            Long memberId,
            int sidoCode,
            int gugunCode,
            String companionLabel,
            List<String> themeLabels,
            String paceLabel,
            int placeCount
    ) {
        String preferenceText = buildPreferenceText(companionLabel, themeLabels, paceLabel, placeCount);
        String preferenceEmbeddingLiteral = embedToVectorLiteral(preferenceText);

        Integer gugunFilter = gugunCode == 0 ? null : gugunCode;
        List<AttractionEmbeddingCandidateRecord> candidates =
                attractionMapper.findCandidatesByPreferenceEmbedding(
                        sidoCode, gugunFilter, preferenceEmbeddingLiteral, ATTRACTION_CANDIDATE_LIMIT
                );

        List<AiCourseGenerationInput.ReferenceCourse> referenceCourses =
                findReferenceCourses(memberId);

        String profileDescription = findProfileDescription(memberId);

        AiCourseGenerationInput input = buildInput(
                sidoCode, gugunCode, companionLabel, themeLabels, paceLabel, placeCount,
                candidates, referenceCourses, profileDescription
        );
        AiCourseGenerationResult result = aiCourseGenerationClient.generate(input);

        return buildPreview(result, candidates);
    }

    private List<AiCourseGenerationInput.ReferenceCourse> findReferenceCourses(Long memberId) {
        if (!courseReader.hasMemberProfileEmbedding(memberId)) {
            return List.of();
        }

        return courseReader.findCandidatesByMemberProfile(memberId, REFERENCE_COURSE_LIMIT)
                .stream()
                .map(candidate -> {
                    List<String> stopTitles = candidate.course().stops().stream()
                            .map(stop -> stop.title() != null ? stop.title() : "")
                            .filter(title -> !title.isBlank())
                            .toList();
                    return new AiCourseGenerationInput.ReferenceCourse(
                            candidate.course().title(),
                            stopTitles
                    );
                })
                .toList();
    }

    private String findProfileDescription(Long memberId) {
        return memberProfileEmbeddingMapper.findProfileDescriptionByMemberId(memberId);
    }

    private String embedToVectorLiteral(String text) {
        EmbeddingModel model = embeddingModelProvider.getIfAvailable();
        if (model == null) {
            throw new IllegalStateException("EmbeddingModel이 사용 불가능합니다.");
        }

        EmbeddingResponse response = model.call(new EmbeddingRequest(List.of(text), null));
        float[] vector = response.getResults().get(0).getOutput();
        return toVectorLiteral(vector);
    }

    private static String toVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(',');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private static String buildPreferenceText(
            String companionLabel,
            List<String> themeLabels,
            String paceLabel,
            int placeCount
    ) {
        String themeText = String.join(", ", themeLabels);
        return companionLabel + " 함께 " + themeText + " 위주로 " + paceLabel + " " + placeCount + "곳";
    }

    private static AiCourseGenerationInput buildInput(
            int sidoCode,
            int gugunCode,
            String companionLabel,
            List<String> themeLabels,
            String paceLabel,
            int placeCount,
            List<AttractionEmbeddingCandidateRecord> candidates,
            List<AiCourseGenerationInput.ReferenceCourse> referenceCourses,
            String profileDescription
    ) {
        String neighborhood = "sido=" + sidoCode + (gugunCode != 0 ? ",gugun=" + gugunCode : "");
        List<AiCourseGenerationInput.AttractionItem> attractionItems = candidates.stream()
                .map(r -> new AiCourseGenerationInput.AttractionItem(
                        r.getId(),
                        r.getTitle(),
                        r.getAddr1(),
                        r.getContentTypeId(),
                        r.getOverview()
                ))
                .toList();

        return new AiCourseGenerationInput(
                neighborhood,
                companionLabel,
                themeLabels,
                paceLabel,
                placeCount,
                attractionItems,
                referenceCourses,
                profileDescription
        );
    }

    private AiCoursePreview buildPreview(
            AiCourseGenerationResult result,
            List<AttractionEmbeddingCandidateRecord> candidates
    ) {
        Map<Long, AttractionRecord> attractionById = fetchAttractionDetails(result.attractionIds());

        List<AiCoursePreview.Stop> stops = result.attractionIds().stream()
                .map(id -> toStop(id, attractionById, candidates))
                .toList();

        return new AiCoursePreview(result.title(), result.reason(), stops);
    }

    private Map<Long, AttractionRecord> fetchAttractionDetails(List<Long> attractionIds) {
        if (attractionIds.isEmpty()) {
            return Map.of();
        }
        return attractionMapper.findByIds(attractionIds).stream()
                .collect(Collectors.toMap(AttractionRecord::id, Function.identity()));
    }

    private static AiCoursePreview.Stop toStop(
            long id,
            Map<Long, AttractionRecord> attractionById,
            List<AttractionEmbeddingCandidateRecord> candidates
    ) {
        AttractionRecord record = attractionById.get(id);
        if (record != null) {
            return new AiCoursePreview.Stop(
                    record.id(), record.title(), record.addr1(), record.firstImage()
            );
        }

        return candidates.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .map(c -> new AiCoursePreview.Stop(c.getId(), c.getTitle(), c.getAddr1(), null))
                .orElse(new AiCoursePreview.Stop(id, null, null, null));
    }
}

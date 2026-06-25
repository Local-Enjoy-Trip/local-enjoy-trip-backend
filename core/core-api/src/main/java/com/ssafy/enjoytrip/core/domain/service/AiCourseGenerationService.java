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
import java.util.ArrayList;
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
            String regionName,
            String companionLabel,
            List<String> themeLabels,
            String paceLabel,
            int placeCount
    ) {
        String preferenceText = buildPreferenceText(companionLabel, themeLabels, paceLabel, placeCount);
        String preferenceEmbeddingLiteral = embedToVectorLiteral(preferenceText);

        // 1. 첫번째 경유지 찾기 (동네로 필터링한 장소중에서 유사도가 가장 높은 것)
        List<AttractionEmbeddingCandidateRecord> firstStopCandidates = new ArrayList<>();
        if (regionName != null && !regionName.isBlank()) {
            // 사용자가 명시적인 동네명을 준 경우, 동네명 조건만으로 검색을 시도합니다.
            firstStopCandidates = attractionMapper.findCandidatesByPreferenceEmbedding(
                    null, null, regionName, preferenceEmbeddingLiteral, 1
            );
        } else {
            firstStopCandidates = attractionMapper.findCandidatesByPreferenceEmbedding(
                    null, null, null, preferenceEmbeddingLiteral, 1
            );
        }

        // 지정한 동네에 관광지가 없는 경우(또는 동네 입력이 애초에 없어서 1차 검색 실패시), 동네 필터 없이 전체에서 찾음
        if (firstStopCandidates.isEmpty()) {
            firstStopCandidates = attractionMapper.findCandidatesByPreferenceEmbedding(
                    null, null, null, preferenceEmbeddingLiteral, 1
            );
        }

        if (firstStopCandidates.isEmpty()) {
            return new AiCoursePreview("", "", List.of());
        }

        AttractionEmbeddingCandidateRecord firstStop = firstStopCandidates.get(0);

        // 2. 속도에 따라 radius 정하기
        double radiusMeters = switch (paceLabel) {
            case "여유롭게" -> 1500.0;
            case "알맞게" -> 3000.0;
            case "알차게" -> 5000.0;
            default -> 3000.0;
        };

        // 3. 첫번째 경유지의 radius 반경 내에서 유사도 검색을 다시 진행하여 후보지 가져오기
        List<AttractionEmbeddingCandidateRecord> remainingCandidates =
                attractionMapper.findCandidatesWithinRadius(
                        null,
                        firstStop.getLatitude(),
                        firstStop.getLongitude(),
                        radiusMeters,
                        preferenceEmbeddingLiteral,
                        firstStop.getId(),
                        ATTRACTION_CANDIDATE_LIMIT
                );

        // 4. 첫번째 경유지와 반경 내 후보지들을 합쳐서 후보 목록 구성
        List<AttractionEmbeddingCandidateRecord> combinedCandidates = new ArrayList<>();
        combinedCandidates.add(firstStop);
        combinedCandidates.addAll(remainingCandidates);

        List<AiCourseGenerationInput.ReferenceCourse> referenceCourses =
                findReferenceCourses(memberId);

        String profileDescription = findProfileDescription(memberId);

        String resolvedRegion = resolveRegionName(regionName, combinedCandidates);

        AiCourseGenerationInput input = buildInput(
                resolvedRegion, companionLabel, themeLabels, paceLabel, placeCount,
                combinedCandidates, referenceCourses, profileDescription
        );
        AiCourseGenerationResult result = aiCourseGenerationClient.generate(input);

        return buildPreview(result, combinedCandidates, firstStop.getId());
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
            String neighborhood,
            String companionLabel,
            List<String> themeLabels,
            String paceLabel,
            int placeCount,
            List<AttractionEmbeddingCandidateRecord> candidates,
            List<AiCourseGenerationInput.ReferenceCourse> referenceCourses,
            String profileDescription
    ) {
        List<AiCourseGenerationInput.AttractionItem> attractionItems = candidates.stream()
                .map(r -> new AiCourseGenerationInput.AttractionItem(
                        r.getId(),
                        r.getTitle(),
                        r.getAddr1(),
                        r.getAddr2(),
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
            List<AttractionEmbeddingCandidateRecord> candidates,
            long firstStopId
    ) {
        List<Long> candidateIds = candidates.stream()
                .map(AttractionEmbeddingCandidateRecord::getId)
                .toList();

        List<Long> resultIds = new ArrayList<>(result.attractionIds());
        
        // Remove firstStopId from its original position if present, to prevent duplicates
        resultIds.remove(firstStopId);
        
        // Force firstStopId at the beginning of the list
        resultIds.add(0, firstStopId);

        List<Long> validAttractionIds = resultIds.stream()
                .filter(candidateIds::contains)
                .toList();

        Map<Long, AttractionRecord> attractionById = fetchAttractionDetails(validAttractionIds);

        List<AiCoursePreview.Stop> stops = validAttractionIds.stream()
                .map(id -> toStop(id, attractionById, candidates))
                .toList();

        return new AiCoursePreview(result.title(), result.reason(), stops);
    }

    private String resolveRegionName(String regionName, List<AttractionEmbeddingCandidateRecord> candidates) {
        // 1. 후보지가 있다면 첫 번째 경유지의 addr2(동네명)를 최우선으로 지역명으로 사용합니다.
        if (candidates != null && !candidates.isEmpty()) {
            AttractionEmbeddingCandidateRecord first = candidates.get(0);
            String addr2 = first.getAddr2();
            if (addr2 != null && !addr2.isBlank()) {
                return addr2;
            }
        }

        // 2. 후보지가 없거나 addr2가 비어있다면, 사용자가 입력했던 regionName을 차선책으로 사용합니다.
        if (regionName != null && !regionName.isBlank()) {
            return regionName;
        }

        // 3. 최후의 수단
        return "동네";
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
            String address = record.addr2() != null && !record.addr2().isBlank() ? record.addr2() : record.addr1();
            return new AiCoursePreview.Stop(
                    record.id(), record.title(), address, record.firstImage()
            );
        }

        return candidates.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .map(c -> {
                    String address = c.getAddr2() != null && !c.getAddr2().isBlank() ? c.getAddr2() : c.getAddr1();
                    return new AiCoursePreview.Stop(c.getId(), c.getTitle(), address, null);
                })
                .orElse(new AiCoursePreview.Stop(id, null, null, null));
    }
}

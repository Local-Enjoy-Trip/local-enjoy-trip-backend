package com.ssafy.enjoytrip.external.briefing;

import com.ssafy.enjoytrip.domain.CourseBriefingCandidate;
import com.ssafy.enjoytrip.domain.NeighborhoodBriefingPrompt;
import com.ssafy.enjoytrip.domain.WeatherSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NeighborhoodBriefingPromptTemplateTest {

    @DisplayName("프롬프트에 지역 날씨와 저장된 코스 제목을 포함한다")
    @Test
    void promptContainsWeatherAndCourseTitles() {
        NeighborhoodBriefingPrompt prompt = new NeighborhoodBriefingPrompt(
                "서울",
                new WeatherSummary("서울", "맑음", 27, 10, "05:10", "19:50"),
                List.of(new CourseBriefingCandidate("course-1", "한강 저녁 산책", "서울"))
        );

        String userPrompt = NeighborhoodBriefingPromptTemplate.userPrompt(prompt);

        assertThat(userPrompt).contains("서울", "맑음", "27도", "한강 저녁 산책");
        assertThat(userPrompt).doesNotContain("계절");
        assertThat(NeighborhoodBriefingPromptTemplate.SYSTEM_PROMPT).contains("courseId", "JSON");
        assertThat(NeighborhoodBriefingPromptTemplate.SYSTEM_PROMPT).doesNotContain("계절");
        assertThat(NeighborhoodBriefingPromptTemplate.SYSTEM_PROMPT)
                .contains("첫 줄", "동네의 현재 상황", "2~3문장");
        assertThat(userPrompt).contains("상황 브리핑", "코스 후보를 바탕으로 할 일");
    }

    @DisplayName("생성 응답은 코드펜스를 제거하고 줄바꿈을 보존하며 길이를 제한한다")
    @Test
    void sanitizeRemovesCodeFencePreservesLineBreaksAndLimitsLength() {
        String sanitized = NeighborhoodBriefingPromptTemplate.sanitize("""
                ```text
                오늘 서울은 맑아요.
                한강 저녁 산책 코스 어떠세요?
                ```
                """, 30);

        assertThat(sanitized).doesNotContain("```");
        assertThat(sanitized).contains("\n");
        assertThat(sanitized.length()).isLessThanOrEqualTo(30);
    }

    @DisplayName("구조화 추천 ID 응답은 문장 계약 위반으로 비운다")
    @Test
    void sanitizeRejectsStructuredRecommendationIds() {
        String sanitized = NeighborhoodBriefingPromptTemplate.sanitize(
                "{\"courseId\":\"course-1\",\"briefing\":\"추천\"}",
                160
        );

        assertThat(sanitized).isBlank();
    }

    @DisplayName("목록형 bullet 응답은 문장 계약 위반으로 비운다")
    @Test
    void sanitizeRejectsBulletRecommendation() {
        String sanitized = NeighborhoodBriefingPromptTemplate.sanitize("""
                오늘은 동네 골목을 걷기 좋아요.
                - 한강 저녁 산책을 추천해요.
                """, 160);

        assertThat(sanitized).isBlank();
    }
}

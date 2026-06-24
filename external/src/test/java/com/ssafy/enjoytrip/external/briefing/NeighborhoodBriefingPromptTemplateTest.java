package com.ssafy.enjoytrip.external.briefing;

import com.ssafy.enjoytrip.external.WeatherBriefingResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NeighborhoodBriefingPromptTemplateTest {

    @DisplayName("프롬프트에 지역·날씨·장소 목록을 포함하고 코스 관련 내용은 없다")
    @Test
    void promptContainsWeatherAndLocalPlaces() {
        NeighborhoodBriefingPromptData prompt = new NeighborhoodBriefingPromptData(
                "장안동",
                new WeatherBriefingResult("장안동", "흐림", 25, 20, "05:20", "19:30", 20, 28),
                List.of(
                        new LocalPlaceData("장안시장", "서울특별시 동대문구 장한로 100", "38"),
                        new LocalPlaceData("중랑천카페", "서울특별시 동대문구 중랑천로 50", "39")
                )
        );

        String userPrompt = NeighborhoodBriefingPromptTemplate.userPrompt(prompt);

        assertThat(userPrompt).contains("장안동", "흐림", "25도", "장안시장", "중랑천카페");
        assertThat(userPrompt).contains("음식점", "카페·쇼핑");
        assertThat(userPrompt).doesNotContain("courseId");
        assertThat(NeighborhoodBriefingPromptTemplate.SYSTEM_PROMPT).contains("네 문장", "JSON");
    }

    @DisplayName("장소 목록이 비어도 프롬프트가 정상 생성된다")
    @Test
    void promptWithNoPlacesOmitsPlaceSection() {
        NeighborhoodBriefingPromptData prompt = new NeighborhoodBriefingPromptData(
                "홍대",
                new WeatherBriefingResult("홍대", "맑음", 27, 0, "05:10", "19:50", 22, 30),
                List.of()
        );

        String userPrompt = NeighborhoodBriefingPromptTemplate.userPrompt(prompt);

        assertThat(userPrompt).contains("홍대", "맑음");
        assertThat(userPrompt).doesNotContain("동네 장소 목록");
    }

    @DisplayName("생성 응답은 코드펜스를 제거하고 줄바꿈을 보존하며 길이를 제한한다")
    @Test
    void sanitizeRemovesCodeFencePreservesLineBreaksAndLimitsLength() {
        String sanitized = NeighborhoodBriefingPromptTemplate.sanitize("""
                ```text
                오늘 서울은 맑아요.
                장안시장에서 간단히 먹고 걸어보세요.
                ```
                """, 40);

        assertThat(sanitized).doesNotContain("```");
        assertThat(sanitized).contains("\n");
        assertThat(sanitized.length()).isLessThanOrEqualTo(40);
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
                - 장안시장을 추천해요.
                """, 160);

        assertThat(sanitized).isBlank();
    }
}

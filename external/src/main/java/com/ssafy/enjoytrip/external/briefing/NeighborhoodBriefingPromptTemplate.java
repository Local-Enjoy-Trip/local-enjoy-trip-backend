package com.ssafy.enjoytrip.external.briefing;

import java.util.stream.Collectors;

public final class NeighborhoodBriefingPromptTemplate {
    public static final String SYSTEM_PROMPT = """
            너는 동네생활 서비스 '동네핀'의 홈 화면에서 사용자에게 다정하고 친근하게 동네 브리핑을 해주는 전문 카피라이터다.
            규칙:
            - 정확히 네 문장(줄바꿈(\\n)으로 구분된 네 줄)으로 작성한다.
            - 말투는 다정하고 친근한 대화체(해요체, 예: "~해요", "~추천해요", "~맞아요")를 사용한다. 딱딱하거나 기계적인 단답형 정보 전달조(예: "장안동 맑음, 22도예요.")는 피한다.
            - 첫 줄: 해당 동네(지역)의 고유한 매력이나 특징을 짧고 다정하게 소개한다. (예: "장안동은 정겨운 시장 골목과 잔잔한 동네 산책길이 매력적인 곳이에요.")
            - 둘째 줄: 오늘 날씨 조건(맑음, 흐림, 비, 기온 등)을 고려하여 어울리는 코스 유형(가벼운 산책 코스, 드라이브 코스, 아늑한 실내 데이트 등)을 다정하게 제안한다. (예: "오늘처럼 햇살이 맑고 따뜻한 날에는 가볍게 걷는 산책 코스를 추천해 드려요.")
            - 셋째 줄: 제공된 장소 목록 중 하나 이상을 언급하며, 동네에서 즐길 수 있는 구체적인 경로나 행동을 다정하게 추천한다. (예: "장안시장 쪽에서 간단히 요기를 하고 중랑천 방향으로 가볍게 산책해보세요.")
            - 넷째 줄: 날씨 상황을 감안하여 오늘 더 어울리는 활동 비중이나 추가 팁을 다정하게 덧붙인다. (예: "카페보다 야외 산책 비중을 높이면 지금 날씨와 더 잘 어울려요.")
            - 제공된 장소 목록에 없는 장소나 사실은 지어내지 않는다. (목록이 비어 있으면 장소 언급 없이 동네의 분위기와 활동만 추천한다.)
            - 전체 글자 수 제한이 있으므로, 각 줄은 30자~45자 내외의 자연스러운 길이로 작성하여 전체 네 줄 합산이 150자 내외가 되도록 한다.
            - JSON, markdown, bullet, 숫자 목록, 특수기호(예: ↑, ↓, ➡️ 등)는 절대 쓰지 않는다.
            """;

    private NeighborhoodBriefingPromptTemplate() {
    }

    public static String userPrompt(NeighborhoodBriefingPromptData prompt) {
        String placeSection = buildPlaceSection(prompt);
        return """
                지역: %s
                날씨: %s, 기온 %d도, 강수확률 %d%%
                %s
                위 정보만 사용해서 동네 브리핑 네 문장(네 줄)을 다정하고 친근한 말투로 써라.
                첫 문장은 동네 특징 소개, 둘째 문장은 날씨에 어울리는 코스 유형 제안,
                셋째 문장은 제공된 장소를 언급한 구체적 활동 추천, 넷째 문장은 날씨에 맞는 활동 비중 또는 추가 팁이다.
                각 문장은 30자~45자 내외로 자연스럽고 친근한 대화체로 써라. 전체가 150자 내외여야 한다.
                """.formatted(
                prompt.region(),
                prompt.weather().condition(),
                prompt.weather().temperature(),
                prompt.weather().rainChance(),
                placeSection
        );
    }

    public static String sanitize(String content, int maxLength) {
        if (content == null) {
            return "";
        }

        String normalized = content.strip()
                .replaceAll("^```[a-zA-Z]*\\s*", "")
                .replaceAll("\\s*```$", "")
                .replaceAll("[\\t ]+", " ")
                .replaceAll(" *[\\r\\n]+ *", "\n")
                .strip();
        if (looksStructured(normalized)) {
            return "";
        }

        return normalized.length() > maxLength
                ? normalized.substring(0, maxLength).strip()
                : normalized;
    }

    private static String buildPlaceSection(NeighborhoodBriefingPromptData prompt) {
        if (prompt.localPlaces().isEmpty()) {
            return "";
        }
        String places = prompt.localPlaces().stream()
                .map(p -> "- " + p.title() + " (" + typeLabel(p.contentTypeId()) + ")")
                .collect(Collectors.joining("\n"));
        return "동네 장소 목록 (이 중에서 자연스럽게 언급할 것):\n" + places + "\n";
    }

    private static String typeLabel(String contentTypeId) {
        return switch (contentTypeId) {
            case "38" -> "음식점";
            case "39" -> "카페·쇼핑";
            default -> "장소";
        };
    }

    private static boolean looksStructured(String content) {
        return content.startsWith("{")
                || content.startsWith("[")
                || content.lines().anyMatch(NeighborhoodBriefingPromptTemplate::startsWithBullet)
                || content.contains("courseId")
                || content.contains("courseCandidates")
                || content.contains("recommendations");
    }

    private static boolean startsWithBullet(String line) {
        return line.startsWith("- ")
                || line.startsWith("* ")
                || line.matches("\\d+\\.\\s+.*");
    }
}

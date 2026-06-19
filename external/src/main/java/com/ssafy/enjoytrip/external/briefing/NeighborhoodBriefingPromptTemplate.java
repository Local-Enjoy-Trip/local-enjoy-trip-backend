package com.ssafy.enjoytrip.external.briefing;


import java.util.stream.Collectors;

public final class NeighborhoodBriefingPromptTemplate {
    public static final String SYSTEM_PROMPT = """
            너는 동네핀 홈의 짧은 동네 브리핑 문장을 쓰는 한국어 카피라이터다.
            규칙:
            - 첫 줄은 동네의 현재 상황을 브리핑하고, 가능하면 그 상황에 맞는 행동 추천까지 담는다.
            - 다음 줄부터는 무엇을 하면 좋을지 2~3문장으로 간단히 추천한다.
            - 전체는 3~4문장으로 쓴다.
            - JSON, markdown, bullet, courseId, 추천 ID, 배열, 리스트를 쓰지 않는다.
            - 저장된 코스 후보 제목 중 하나를 자연스럽게 언급한다.
            - 후보 제목에 없는 장소나 세부 사실을 새로 지어내지 않는다.
            - 과장하지 말고 날씨 맥락을 가볍게 반영한다.
            """;

    private NeighborhoodBriefingPromptTemplate() {
    }

    public static String userPrompt(NeighborhoodBriefingPromptData prompt) {
        return """
                지역: %s
                날씨: %s, 기온 %d도, 강수확률 %d%%
                저장된 공개 코스 후보:
                %s

                위 정보만 사용해서 홈 상단에 보여줄 자연스러운 동네 브리핑을 작성해라.
                첫 줄은 "오늘은 햇빛이 강하지 않아 동네 골목을 천천히 걷기 좋아요."처럼 상황 브리핑으로 시작해라.
                다음 줄부터는 "장안시장 쪽에서 간단히 먹고, 중랑천 방향으로 짧게 이어지는 코스를 추천해요."처럼
                코스 후보를 바탕으로 할 일을 2~3문장 추천해라.
                """.formatted(
                prompt.region(),
                prompt.weather().condition(),
                prompt.weather().temperature(),
                prompt.weather().rainChance(),
                courseTitles(prompt)
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

    private static String courseTitles(NeighborhoodBriefingPromptData prompt) {
        return prompt.courseCandidates().stream()
                .map(CourseBriefingCandidateData::title)
                .map(title -> "- " + title)
                .collect(Collectors.joining("\n"));
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

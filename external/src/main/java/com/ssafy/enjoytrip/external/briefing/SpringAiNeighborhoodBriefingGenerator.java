package com.ssafy.enjoytrip.external.briefing;


import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAiNeighborhoodBriefingGenerator {
    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final GmsNeighborhoodBriefingProperties properties;

    public String generate(NeighborhoodBriefingPromptData prompt) {
        properties.assertLiveReady();
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new IllegalStateException("Spring AI ChatClient.Builder를 사용할 수 없습니다.");
        }

        String content = builder.build()
                .prompt()
                .system(NeighborhoodBriefingPromptTemplate.SYSTEM_PROMPT)
                .user(NeighborhoodBriefingPromptTemplate.userPrompt(prompt))
                .call()
                .content();

        return NeighborhoodBriefingPromptTemplate.sanitize(content, properties.getMaxLength());
    }
}

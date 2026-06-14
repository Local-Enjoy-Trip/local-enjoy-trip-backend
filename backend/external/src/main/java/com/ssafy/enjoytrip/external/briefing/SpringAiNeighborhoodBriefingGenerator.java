package com.ssafy.enjoytrip.external.briefing;

import com.ssafy.enjoytrip.domain.NeighborhoodBriefingPrompt;
import com.ssafy.enjoytrip.repository.NeighborhoodBriefingGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAiNeighborhoodBriefingGenerator implements NeighborhoodBriefingGenerator {
    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final GmsNeighborhoodBriefingProperties properties;

    @Override
    public String generate(NeighborhoodBriefingPrompt prompt) {
        properties.assertLiveReady();
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new IllegalStateException("Spring AI ChatClient.Builder is not available.");
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

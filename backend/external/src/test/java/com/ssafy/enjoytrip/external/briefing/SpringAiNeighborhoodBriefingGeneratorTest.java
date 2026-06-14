package com.ssafy.enjoytrip.external.briefing;

import com.ssafy.enjoytrip.domain.CourseBriefingCandidate;
import com.ssafy.enjoytrip.domain.NeighborhoodBriefingPrompt;
import com.ssafy.enjoytrip.domain.WeatherSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SpringAiNeighborhoodBriefingGeneratorTest {

    @DisplayName("Spring AI ChatClient 호출 결과를 브리핑 문장으로 반환한다")
    @Test
    void generatesBriefingWithSpringAiChatClient() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        when(builder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("오늘 서울은 맑아요.\n한강 저녁 산책 코스 어떠세요?");
        GmsNeighborhoodBriefingProperties properties = liveProperties();
        SpringAiNeighborhoodBriefingGenerator generator = new SpringAiNeighborhoodBriefingGenerator(
                provider(builder),
                properties
        );

        String result = generator.generate(prompt());

        assertThat(result).isEqualTo("오늘 서울은 맑아요.\n한강 저녁 산책 코스 어떠세요?");
        ArgumentCaptor<String> userPromptCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).user(userPromptCaptor.capture());
        assertThat(userPromptCaptor.getValue()).contains("서울", "맑음", "한강 저녁 산책");
        assertThat(userPromptCaptor.getValue()).doesNotContain("계절", "여름");
    }

    @DisplayName("GMS_KEY가 비어 있으면 ChatClient를 호출하지 않고 fallback 가능한 예외를 던진다")
    @Test
    void throwsBeforeCallingChatClientWhenGmsKeyIsMissing() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        GmsNeighborhoodBriefingProperties properties = new GmsNeighborhoodBriefingProperties();
        SpringAiNeighborhoodBriefingGenerator generator = new SpringAiNeighborhoodBriefingGenerator(
                provider(builder),
                properties
        );

        assertThatThrownBy(() -> generator.generate(prompt()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GMS API key is missing");
        verifyNoInteractions(builder);
    }

    @DisplayName("Spring AI ChatClient.Builder가 없으면 fallback 가능한 예외를 던진다")
    @Test
    void throwsWhenChatClientBuilderIsNotAvailable() {
        SpringAiNeighborhoodBriefingGenerator generator = new SpringAiNeighborhoodBriefingGenerator(
                provider(null),
                liveProperties()
        );

        assertThatThrownBy(() -> generator.generate(prompt()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ChatClient.Builder is not available");
    }

    private static NeighborhoodBriefingPrompt prompt() {
        return new NeighborhoodBriefingPrompt(
                "서울",
                new WeatherSummary("서울", "맑음", 27, 10, "05:10", "19:50"),
                List.of(new CourseBriefingCandidate("course-1", "한강 저녁 산책", "서울"))
        );
    }

    private static GmsNeighborhoodBriefingProperties liveProperties() {
        GmsNeighborhoodBriefingProperties properties = new GmsNeighborhoodBriefingProperties();
        properties.setApiKey("test-key");
        properties.setMaxLength(160);
        return properties;
    }

    private static ObjectProvider<ChatClient.Builder> provider(ChatClient.Builder builder) {
        return new ObjectProvider<>() {
            @Override
            public ChatClient.Builder getObject(Object... args) throws BeansException {
                return builder;
            }

            @Override
            public ChatClient.Builder getObject() throws BeansException {
                return builder;
            }

            @Override
            public ChatClient.Builder getIfAvailable() throws BeansException {
                return builder;
            }
        };
    }
}

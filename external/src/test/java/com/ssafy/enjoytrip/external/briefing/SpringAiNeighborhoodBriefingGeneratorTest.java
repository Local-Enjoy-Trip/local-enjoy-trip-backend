package com.ssafy.enjoytrip.external.briefing;

import com.ssafy.enjoytrip.external.WeatherBriefingResult;
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
        when(responseSpec.content()).thenReturn(
                "오늘 장안동은 흐리고 25도예요.\n장안시장에서 간단히 먹고 중랑천 방향으로 걸어보세요.\n카페보다 산책 비중을 높이면 지금 날씨와 더 잘 맞아요.");
        GmsNeighborhoodBriefingProperties properties = liveProperties();
        SpringAiNeighborhoodBriefingGenerator generator = new SpringAiNeighborhoodBriefingGenerator(
                provider(builder),
                properties
        );

        String result = generator.generate(prompt());

        assertThat(result).contains("장안동", "장안시장");
        ArgumentCaptor<String> userPromptCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).user(userPromptCaptor.capture());
        assertThat(userPromptCaptor.getValue()).contains("장안동", "흐림", "장안시장");
        assertThat(userPromptCaptor.getValue()).doesNotContain("courseId");
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
                .hasMessageContaining("GMS API 키가 없습니다");
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
                .hasMessageContaining("ChatClient.Builder를 사용할 수 없습니다");
    }

    private static NeighborhoodBriefingPromptData prompt() {
        return new NeighborhoodBriefingPromptData(
                "장안동",
                new WeatherBriefingResult("장안동", "흐림", 25, 20, "05:20", "19:30", 20, 28),
                List.of(new LocalPlaceData("장안시장", "서울특별시 동대문구 장한로 100", "38"))
        );
    }

    private static GmsNeighborhoodBriefingProperties liveProperties() {
        GmsNeighborhoodBriefingProperties properties = new GmsNeighborhoodBriefingProperties();
        properties.setApiKey("test-key");
        properties.setMaxLength(200);
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

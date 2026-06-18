package com.ssafy.enjoytrip.core.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.ssafy.enjoytrip.core.domain.external.AttractionPopularityClient;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("service")
class DbBackedTravelDataServicesTest {

    @Nested
    class AttractionServiceTests {
        @DisplayName("AttractionService는 Store 없이 popularity client와 jOOQ DSLContext로 생성된다")
        @Test
        void constructsWithDirectDbCoreDependencies() {
            AttractionService service = new AttractionService(
                    mock(AttractionPopularityClient.class),
                    mock(DSLContext.class)
            );

            assertThat(service).isNotNull();
        }
    }

    @Nested
    class EvChargerServiceTests {
        @DisplayName("EvChargerService는 Store 없이 jOOQ DSLContext로 생성된다")
        @Test
        void constructsWithDirectDslContext() {
            assertThat(new EvChargerService(mock(DSLContext.class))).isNotNull();
        }
    }

    @Nested
    class NewsServiceTests {
        @DisplayName("NewsService는 Store 없이 jOOQ DSLContext로 생성된다")
        @Test
        void constructsWithDirectDslContext() {
            assertThat(new NewsService(mock(DSLContext.class))).isNotNull();
        }
    }
}

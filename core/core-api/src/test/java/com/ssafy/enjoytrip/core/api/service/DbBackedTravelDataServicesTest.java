package com.ssafy.enjoytrip.core.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.ssafy.enjoytrip.external.ClickHouseAttractionPopularityClient;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.EvChargerMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NewsMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("service")
class DbBackedTravelDataServicesTest {

    @Nested
    class AttractionServiceTests {
        @DisplayName("AttractionService는 MyBatis mapper와 popularity client로 생성된다")
        @Test
        void constructsWithMyBatisMapperDependencies() {
            AttractionService service = new AttractionService(
                    mock(ClickHouseAttractionPopularityClient.class),
                    mock(AttractionMapper.class)
            );

            assertThat(service).isNotNull();
        }
    }

    @Nested
    class EvChargerServiceTests {
        @DisplayName("EvChargerService는 MyBatis mapper로 생성된다")
        @Test
        void constructsWithMyBatisMapper() {
            assertThat(new EvChargerService(mock(EvChargerMapper.class))).isNotNull();
        }
    }

    @Nested
    class NewsServiceTests {
        @DisplayName("NewsService는 MyBatis mapper로 생성된다")
        @Test
        void constructsWithMyBatisMapper() {
            assertThat(new NewsService(mock(NewsMapper.class))).isNotNull();
        }
    }
}

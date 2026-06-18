package com.ssafy.enjoytrip.storage.db.core.mybatis;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

class MyBatisMapperXmlTest {
    @Test
    @DisplayName("MyBatis mapper XML은 SqlSessionFactory 생성 전에 파싱 가능하다")
    void mapperXmlFilesAreParseable() throws Exception {
        Resource[] mapperResources = new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mybatis/mapper/**/*.xml");
        Configuration configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);

        assertThat(mapperResources).isNotEmpty();
        for (Resource mapperResource : mapperResources) {
            try (InputStream inputStream = mapperResource.getInputStream()) {
                XMLMapperBuilder builder = new XMLMapperBuilder(
                        inputStream,
                        configuration,
                        mapperResource.toString(),
                        configuration.getSqlFragments()
                );
                builder.parse();
            }
        }
    }
}

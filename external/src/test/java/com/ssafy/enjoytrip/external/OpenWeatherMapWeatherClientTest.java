package com.ssafy.enjoytrip.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenWeatherMapWeatherClientTest {

    @DisplayName("API 키가 없으면 HTTP 호출 전에 실패한다")
    @Test
    void missingApiKeyFailsBeforeHttpCall() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenWeatherMapWeatherClient repository = new OpenWeatherMapWeatherClient(builder.build(), "");

        assertThatThrownBy(repository::findWeatherBriefings)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenWeatherMap API 키가 없습니다");
        server.verify();
    }

    @DisplayName("현재 날씨와 예보 응답을 날씨 요약 행으로 매핑한다")
    @Test
    void mapsCurrentAndForecastResponsesToWeatherBriefingResultRows() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenWeatherMapWeatherClient repository = new OpenWeatherMapWeatherClient(
                builder.build(),
                "test-key"
        );

        for (int i = 0; i < 3; i++) {
            server.expect(request -> assertThat(request.getURI().toString())
                            .contains("/data/2.5/weather")
                            .contains("appid=test-key")
                            .contains("units=metric")
                            .contains("lang=kr")
                            .contains("lat=")
                            .contains("lon="))
                    .andRespond(withSuccess("""
                            {"weather":[{"main":"Clear","description":"맑음"}],
                             "main":{"temp":18.4,"temp_min":15.0,"temp_max":22.0},
                             "sys":{"sunrise":1716841380,"sunset":1716892380}}
                            """, MediaType.APPLICATION_JSON));
            server.expect(request -> assertThat(request.getURI().toString())
                            .contains("/data/2.5/forecast")
                            .contains("appid=test-key")
                            .contains("units=metric")
                            .contains("lang=kr")
                            .contains("cnt=1")
                            .contains("lat=")
                            .contains("lon="))
                    .andRespond(withSuccess("""
                            {"list":[{"pop":0.7,"main":{"temp":20.0},
                             "weather":[{"main":"Rain","description":"비"}]}]}
                            """, MediaType.APPLICATION_JSON));
        }

        List<WeatherBriefingResult> result = repository.findWeatherBriefings();

        assertThat(result)
                .extracting(WeatherBriefingResult::region)
                .containsExactly("서울", "부산", "제주");
        assertThat(result)
                .extracting(WeatherBriefingResult::condition)
                .containsExactly("맑음", "맑음", "맑음");
        assertThat(result)
                .extracting(WeatherBriefingResult::temperature)
                .containsExactly(18, 18, 18);
        assertThat(result)
                .extracting(WeatherBriefingResult::rainChance)
                .containsExactly(70, 70, 70);
        assertThat(result)
                .extracting(WeatherBriefingResult::tempMin)
                .containsExactly(15, 15, 15);
        assertThat(result)
                .extracting(WeatherBriefingResult::tempMax)
                .containsExactly(22, 22, 22);
        assertThat(result)
                .allSatisfy(row -> {
                    assertThat(row.sunrise()).matches("\\d{2}:\\d{2}");
                    assertThat(row.sunset()).matches("\\d{2}:\\d{2}");
                });
        server.verify();
    }

    @DisplayName("HTTP 실패는 제어된 실패로 처리한다")
    @Test
    void httpFailureIsControlledFailure() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenWeatherMapWeatherClient repository = new OpenWeatherMapWeatherClient(
                builder.build(),
                "test-key"
        );
        server.expect(request -> assertThat(request.getURI().toString()).contains("/data/2.5/weather"))
                .andRespond(withServerError());

        assertThatThrownBy(repository::findWeatherBriefings)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenWeatherMap API 호출에 실패했습니다");
        server.verify();
    }

    @DisplayName("잘못된 현재 날씨 JSON은 제어된 실패로 처리한다")
    @Test
    void malformedCurrentJsonIsControlledFailure() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenWeatherMapWeatherClient repository = new OpenWeatherMapWeatherClient(
                builder.build(),
                "test-key"
        );
        server.expect(request -> assertThat(request.getURI().toString()).contains("/data/2.5/weather"))
                .andRespond(withSuccess("{not-json", MediaType.APPLICATION_JSON));
        server.expect(request -> assertThat(request.getURI().toString()).contains("/data/2.5/forecast"))
                .andRespond(withSuccess("""
                        {"list":[{"pop":0.1}]}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(repository::findWeatherBriefings)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenWeatherMap API 응답을 파싱하지 못했습니다");
        server.verify();
    }

    @DisplayName("예보 목록 누락은 core 대체 처리를 위한 제어된 실패로 처리한다")
    @Test
    void missingForecastListIsControlledFailureForCoreFallbackCompletion() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenWeatherMapWeatherClient repository = new OpenWeatherMapWeatherClient(
                builder.build(),
                "test-key"
        );
        server.expect(request -> assertThat(request.getURI().toString()).contains("/data/2.5/weather"))
                .andRespond(withSuccess("""
                        {"weather":[{"main":"Clouds","description":"구름 많음"}],
                         "main":{"temp":21},
                         "sys":{"sunrise":1716841380,"sunset":1716892380}}
                        """, MediaType.APPLICATION_JSON));
        server.expect(request -> assertThat(request.getURI().toString()).contains("/data/2.5/forecast"))
                .andRespond(withSuccess("""
                        {"cod":"200"}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(repository::findWeatherBriefings)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenWeatherMap API 응답을 파싱하지 못했습니다");
        server.verify();
    }

    @DisplayName("좌표를 기준으로 날씨와 6시간 예보를 성공적으로 조회하고 매핑한다")
    @Test
    void findsWeatherWithForecastSuccessfully() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenWeatherMapWeatherClient repository = new OpenWeatherMapWeatherClient(
                builder.build(),
                "test-key"
        );

        server.expect(request -> assertThat(request.getURI().toString())
                        .contains("/data/2.5/weather")
                        .contains("lat=37.5")
                        .contains("lon=127.0"))
                .andRespond(withSuccess("""
                        {"weather":[{"main":"Clear","description":"맑음"}],
                         "main":{"temp":22.5,"temp_min":19.0,"temp_max":26.0},
                         "sys":{"sunrise":1716841380,"sunset":1716892380}}
                        """, MediaType.APPLICATION_JSON));

        server.expect(request -> assertThat(request.getURI().toString())
                        .contains("/data/2.5/forecast")
                        .contains("lat=37.5")
                        .contains("lon=127.0")
                        .contains("cnt=2"))
                .andRespond(withSuccess("""
                        {"list":[
                          {"dt":1716844980,"pop":0.4,"main":{"temp":23.0},"weather":[{"main":"Clear","description":"맑음"}]},
                          {"dt":1716855780,"pop":0.6,"main":{"temp":21.0},"weather":[{"main":"Rain","description":"비"}]}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        WeatherBriefingWithForecast result = repository.findWeatherWithForecast(37.5, 127.0, "역삼동");

        assertThat(result.current().region()).isEqualTo("역삼동");
        assertThat(result.current().condition()).isEqualTo("맑음");
        assertThat(result.current().temperature()).isEqualTo(23);
        assertThat(result.current().rainChance()).isEqualTo(40);
        assertThat(result.current().tempMin()).isEqualTo(19);
        assertThat(result.current().tempMax()).isEqualTo(26);

        assertThat(result.forecasts()).hasSize(2);
        assertThat(result.forecasts().get(0).condition()).isEqualTo("맑음");
        assertThat(result.forecasts().get(0).temperature()).isEqualTo(23);
        assertThat(result.forecasts().get(0).rainChance()).isEqualTo(40);
        assertThat(result.forecasts().get(1).condition()).isEqualTo("비");
        assertThat(result.forecasts().get(1).temperature()).isEqualTo(21);
        assertThat(result.forecasts().get(1).rainChance()).isEqualTo(60);

        server.verify();
    }
}

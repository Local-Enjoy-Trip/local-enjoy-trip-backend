package com.ssafy.enjoytrip.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenWeatherMapWeatherClientTest {

    @DisplayName("API 키가 없으면 HTTP 호출 전에 실패한다")
    @Test
    void missingApiKeyFailsBeforeHttpCall() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenWeatherMapWeatherClient repository = new OpenWeatherMapWeatherClient(builder.build(), "");

        assertThatThrownBy(() -> repository.findWeatherWithForecast(37.5, 127.0, "역삼동"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenWeatherMap API 키가 없습니다");
        server.verify();
    }

    @DisplayName("좌표 날씨는 1시간 단위 6개 예보로 매핑한다")
    @Test
    void findsWeatherWithForecastSuccessfully() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenWeatherMapWeatherClient repository = new OpenWeatherMapWeatherClient(
                builder.build(),
                "test-key"
        );

        server.expect(request -> assertThat(request.getURI().toString())
                        .contains("/data/3.0/onecall")
                        .contains("lat=37.5")
                        .contains("lon=127.0")
                        .contains("appid=test-key")
                        .contains("units=metric")
                        .contains("lang=kr")
                        .contains("exclude=minutely,alerts"))
                .andRespond(withSuccess("""
                        {"current":{
                           "dt":1716841380,
                           "sunrise":1716841380,
                           "sunset":1716892380,
                           "temp":22.5,
                           "weather":[{"main":"Clear","description":"맑음"}]
                         },
                         "hourly":[
                          {
                            "dt":1716841380,
                            "pop":0.4,
                            "temp":23.0,
                            "weather":[{"main":"Clear","description":"맑음"}]
                          },
                          {
                            "dt":1716844980,
                            "pop":0.1,
                            "temp":24.0,
                            "weather":[{"main":"Clouds","description":"구름 많음"}]
                          },
                          {
                            "dt":1716848580,
                            "pop":0.2,
                            "temp":25.0,
                            "weather":[{"main":"Clear","description":"맑음"}]
                          },
                          {
                            "dt":1716852180,
                            "pop":0.3,
                            "temp":26.0,
                            "weather":[{"main":"Clear","description":"맑음"}]
                          },
                          {
                            "dt":1716855780,
                            "pop":0.4,
                            "temp":27.0,
                            "weather":[{"main":"Rain","description":"비"}]
                          },
                          {
                            "dt":1716859380,
                            "pop":0.5,
                            "temp":28.0,
                            "weather":[{"main":"Rain","description":"비"}]
                          },
                          {
                            "dt":1716862980,
                            "pop":0.6,
                            "temp":29.0,
                            "weather":[{"main":"Clear","description":"맑음"}]
                          }
                         ],
                         "daily":[
                          {"temp":{"min":19.0,"max":29.0}}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        WeatherBriefingWithForecast result = repository.findWeatherWithForecast(37.5, 127.0, "역삼동");

        assertThat(result.current().region()).isEqualTo("역삼동");
        assertThat(result.current().condition()).isEqualTo("맑음");
        assertThat(result.current().temperature()).isEqualTo(23);
        assertThat(result.current().rainChance()).isEqualTo(40);
        assertThat(result.current().tempMin()).isEqualTo(19);
        assertThat(result.current().tempMax()).isEqualTo(29);
        assertThat(result.forecasts()).hasSize(6);
        assertThat(result.forecasts())
                .extracting(WeatherForecastResult::temperature)
                .containsExactly(24, 25, 26, 27, 28, 29);
        assertThat(result.forecasts().get(0).condition()).isEqualTo("구름 많음");
        assertThat(result.forecasts().get(0).rainChance()).isEqualTo(10);
        assertThat(result.forecasts().get(3).condition()).isEqualTo("비");
        assertThat(result.forecasts().get(5).rainChance()).isEqualTo(60);

        server.verify();
    }
}

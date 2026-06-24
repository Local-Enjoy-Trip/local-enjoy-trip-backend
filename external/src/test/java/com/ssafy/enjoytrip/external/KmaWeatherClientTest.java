package com.ssafy.enjoytrip.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KmaWeatherClientTest {

    @DisplayName("API 키가 없으면 HTTP 호출 전에 실패한다")
    @Test
    void missingApiKeyFailsBeforeHttpCall() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KmaWeatherClient repository = new KmaWeatherClient(builder.build(), "");

        assertThatThrownBy(() -> repository.findWeatherWithForecast(37.5, 127.0, "역삼동"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("기상청 API 키가 없습니다");
        server.verify();
    }

    @DisplayName("좌표 날씨는 기상청 초단기실황 및 초단기예보를 조합해 6개 예보로 매핑한다")
    @Test
    void findsWeatherWithForecastSuccessfully() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KmaWeatherClient repository = new KmaWeatherClient(
                builder.build(),
                "test-key"
        );

        String ncstResponseJson = """
                {
                  "response": {
                    "header": {
                      "resultCode": "00",
                      "resultMsg": "NORMAL_SERVICE"
                    },
                    "body": {
                      "dataType": "JSON",
                      "items": {
                        "item": [
                          {
                            "category": "T1H",
                            "obsrValue": "22.5"
                          },
                          {
                            "category": "PTY",
                            "obsrValue": "0"
                          }
                        ]
                      }
                    }
                  }
                }
                """;

        String fcstResponseJson = """
                {
                  "response": {
                    "header": {
                      "resultCode": "00",
                      "resultMsg": "NORMAL_SERVICE"
                    },
                    "body": {
                      "dataType": "JSON",
                      "items": {
                        "item": [
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "1600",
                            "category": "T1H",
                            "fcstValue": "23"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "1600",
                            "category": "SKY",
                            "fcstValue": "1"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "1600",
                            "category": "PTY",
                            "fcstValue": "0"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "1700",
                            "category": "T1H",
                            "fcstValue": "24"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "1700",
                            "category": "SKY",
                            "fcstValue": "3"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "1700",
                            "category": "PTY",
                            "fcstValue": "0"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "1800",
                            "category": "T1H",
                            "fcstValue": "25"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "1800",
                            "category": "SKY",
                            "fcstValue": "1"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "1800",
                            "category": "PTY",
                            "fcstValue": "0"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "1900",
                            "category": "T1H",
                            "fcstValue": "26"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "1900",
                            "category": "SKY",
                            "fcstValue": "1"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "1900",
                            "category": "PTY",
                            "fcstValue": "0"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "2000",
                            "category": "T1H",
                            "fcstValue": "27"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "2000",
                            "category": "SKY",
                            "fcstValue": "4"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "2000",
                            "category": "PTY",
                            "fcstValue": "1"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "2100",
                            "category": "T1H",
                            "fcstValue": "28"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "2100",
                            "category": "SKY",
                            "fcstValue": "1"
                          },
                          {
                            "fcstDate": "20260624",
                            "fcstTime": "2100",
                            "category": "PTY",
                            "fcstValue": "0"
                          }
                        ]
                      }
                    }
                  }
                }
                """;

        server.expect(request -> assertThat(request.getURI().toString())
                        .contains("/getUltraSrtNcst")
                        .contains("nx=60")
                        .contains("ny=127")
                        .contains("serviceKey=test-key"))
                .andRespond(withSuccess(ncstResponseJson, MediaType.APPLICATION_JSON));

        server.expect(request -> assertThat(request.getURI().toString())
                        .contains("/getUltraSrtFcst")
                        .contains("nx=60")
                        .contains("ny=127")
                        .contains("serviceKey=test-key"))
                .andRespond(withSuccess(fcstResponseJson, MediaType.APPLICATION_JSON));

        WeatherBriefingWithForecast result = repository.findWeatherWithForecast(37.5665, 126.9780, "역삼동");

        assertThat(result.current().region()).isEqualTo("역삼동");
        assertThat(result.current().condition()).isEqualTo("맑음"); // 예보의 16시(첫번째) 하늘상태(1=맑음)를 반영하여 맑음이 됨
        assertThat(result.current().temperature()).isEqualTo(23);
        assertThat(result.current().rainChance()).isEqualTo(0);
        assertThat(result.current().tempMin()).isEqualTo(23);
        assertThat(result.current().tempMax()).isEqualTo(28);

        assertThat(result.forecasts()).hasSize(6);
        assertThat(result.forecasts())
                .extracting(WeatherForecastResult::temperature)
                .containsExactly(23, 24, 25, 26, 27, 28);

        assertThat(result.forecasts().get(0).condition()).isEqualTo("맑음");
        assertThat(result.forecasts().get(0).rainChance()).isEqualTo(0);
        assertThat(result.forecasts().get(1).condition()).isEqualTo("구름 많음");
        assertThat(result.forecasts().get(4).condition()).isEqualTo("비");
        assertThat(result.forecasts().get(4).rainChance()).isEqualTo(80);

        server.verify();
    }
}

package com.ssafy.enjoytrip.external;

import com.ssafy.enjoytrip.external.ExternalClientTestSupport.FakeHttpClient;
import com.ssafy.enjoytrip.domain.ChargerItem;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvChargerClientTest {
    @Test
    void findChargersMapsXmlFiltersKeywordAndClampsPaging() throws Exception {
        FakeHttpClient http = new FakeHttpClient().enqueue(200, """
                <response><body><items>
                  <item>
                    <statId>ST001</statId><statNm>강남 충전소</statNm><chgerId>01</chgerId>
                    <chgerType>06</chgerType>
                    <addr>서울 강남구 테헤란로</addr><location>지하 1층</location><lat>37.5</lat><lng>127.0</lng>
                    <useTime>24시간</useTime><busiNm>환경부</busiNm><busiCall>1661</busiCall><stat>2</stat>
                  </item>
                  <item>
                    <statId>ST002</statId><statNm>부산 충전소</statNm><lat>bad</lat><lng></lng>
                  </item>
                </items></body></response>
                """);
        EvChargerClient client = new EvChargerClient(http, "ev-key");

        List<ChargerItem> results = client.findChargers("11", "강남", -1, 999);

        assertThat(results).containsExactly(new ChargerItem(
                "ST001", "강남 충전소", "01", "06", "서울 강남구 테헤란로", "지하 1층",
                37.5, 127.0, "24시간", "환경부", "1661", "2"
        ));
        String uri = URLDecoder.decode(http.requests().getFirst().uri().toString(), StandardCharsets.UTF_8);
        assertThat(uri)
                .contains("serviceKey=ev-key")
                .contains("pageNo=1")
                .contains("numOfRows=500")
                .contains("zcode=11");
    }

    @Test
    void findChargersKeepsAllRowsWhenKeywordBlankAndDefaultsInvalidCoordinates() throws Exception {
        FakeHttpClient http = new FakeHttpClient().enqueue(200, """
                <response><body><items>
                  <item><statNm>첫번째</statNm><lat></lat><lng>bad</lng></item>
                  <item><statNm>두번째</statNm><lat>35.1</lat><lng>129.1</lng></item>
                </items></body></response>
                """);
        EvChargerClient client = new EvChargerClient(http, "ev-key");

        List<ChargerItem> results = client.findChargers("", "", 3, 1);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).lat()).isZero();
        assertThat(results.get(0).lng()).isZero();
        assertThat(results.get(1).lat()).isEqualTo(35.1);
        assertThat(results.get(1).lng()).isEqualTo(129.1);
        String uri = URLDecoder.decode(http.requests().getFirst().uri().toString(), StandardCharsets.UTF_8);
        assertThat(uri)
                .contains("pageNo=3")
                .contains("numOfRows=10")
                .doesNotContain("zcode=");
    }

    @Test
    void throwsWhenApiKeyMissingWithoutCallingHttp() {
        FakeHttpClient http = new FakeHttpClient();
        EvChargerClient client = new EvChargerClient(http, "");

        assertThatThrownBy(() -> client.findChargers("11", "", 1, 10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EV charger API key is missing");
        assertThat(http.requests()).isEmpty();
    }

    @Test
    void throwsIOExceptionForHttpErrorMalformedXmlAndTransportFailure() {
        EvChargerClient httpErrorClient = new EvChargerClient(
                new FakeHttpClient().enqueue(500, "error"),
                "ev-key"
        );

        assertThatThrownBy(() -> httpErrorClient.findChargers("", "", 1, 10))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("EV API HTTP error: 500");

        EvChargerClient malformedClient = new EvChargerClient(
                new FakeHttpClient().enqueue(200, "<response><item>"),
                "ev-key"
        );

        assertThatThrownBy(() -> malformedClient.findChargers("", "", 1, 10))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to parse EV charger API response");

        EvChargerClient transportClient = new EvChargerClient(
                new FakeHttpClient().enqueueIOException("offline"),
                "ev-key"
        );

        assertThatThrownBy(() -> transportClient.findChargers("", "", 1, 10))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("offline");
    }
}

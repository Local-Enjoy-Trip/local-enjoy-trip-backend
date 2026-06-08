package com.ssafy.enjoytrip.external;

import com.ssafy.enjoytrip.domain.ChargerItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class EvChargerClient {
    private static final String CHARGER_URL = "http://apis.data.go.kr/B552584/EvCharger/getChargerInfo";

    private final HttpClient client;
    private final String apiKey;

    public EvChargerClient(
            HttpClient client,
            @Value("${enjoytrip.external.ev-charger.api-key:}") String apiKey
    ) {
        this.client = client;
        this.apiKey = apiKey;
    }

    public List<ChargerItem> findChargers(String zcode, String keyword, int pageNo, int numOfRows)
            throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "EV charger API key is missing. Set enjoytrip.external.ev-charger.api-key."
            );
        }

        StringBuilder url = new StringBuilder(CHARGER_URL)
                .append("?serviceKey=").append(apiKey)
                .append("&pageNo=").append(Math.max(1, pageNo))
                .append("&numOfRows=").append(Math.max(10, Math.min(500, numOfRows)));
        if (zcode != null && !zcode.isBlank()) {
            url.append("&zcode=").append(URLEncoder.encode(zcode, StandardCharsets.UTF_8));
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(url.toString())).GET().build();
        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("EV API HTTP error: " + response.statusCode());
        }
        return parse(response.body(), keyword);
    }

    private List<ChargerItem> parse(String xml, String keyword) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document document = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            String keywordLower = "";
            if (keyword != null) {
                keywordLower = keyword.trim().toLowerCase();
            }
            NodeList itemNodes = document.getElementsByTagName("item");
            List<ChargerItem> rows = new ArrayList<>();
            for (int i = 0; i < itemNodes.getLength(); i++) {
                Element item = (Element) itemNodes.item(i);
                ChargerItem row = new ChargerItem(
                        text(item, "statId"),
                        text(item, "statNm"),
                        text(item, "chgerId"),
                        text(item, "chgerType"),
                        text(item, "addr"),
                        text(item, "location"),
                        parseDouble(text(item, "lat")),
                        parseDouble(text(item, "lng")),
                        text(item, "useTime"),
                        text(item, "busiNm"),
                        text(item, "busiCall"),
                        text(item, "stat")
                );
                if (!keywordLower.isEmpty()) {
                    String hay = (row.statNm() + " " + row.addr() + " " + row.location()).toLowerCase();
                    if (!hay.contains(keywordLower)) continue;
                }
                rows.add(row);
            }
            return rows;
        } catch (Exception ex) {
            throw new IOException("Failed to parse EV charger API response", ex);
        }
    }

    private static String text(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0 || list.item(0) == null) return "";
        String value = list.item(0).getTextContent();
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static Double parseDouble(String value) {
        try {
            if (value == null || value.isBlank()) {
                return 0.0;
            }
            return Double.parseDouble(value);
        } catch (Exception ex) {
            return 0.0;
        }
    }

}

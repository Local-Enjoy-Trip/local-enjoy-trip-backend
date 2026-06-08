package com.ssafy.enjoytrip.external;

import com.ssafy.enjoytrip.domain.Attraction;
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
public class TourApiClient {
    private static final String TOUR_API_BASE = "https://apis.data.go.kr/B551011/KorService2";
    private static final int DEFAULT_ROWS = 200;

    private final HttpClient httpClient;
    private final String serviceKey;

    public TourApiClient(
            HttpClient httpClient,
            @Value("${enjoytrip.external.tour-api.service-key:}") String serviceKey
    ) {
        this.httpClient = httpClient;
        this.serviceKey = serviceKey;
    }

    public List<Attraction> search(String areaCode, String sigunguCode, String contentTypeId, String keyword)
            throws IOException, InterruptedException {
        if (notBlank(keyword)) {
            return callKeywordSearch(keyword, areaCode, sigunguCode, contentTypeId);
        }
        return callAreaBasedList(areaCode, sigunguCode, contentTypeId);
    }

    public List<Attraction> searchAround(
            String mapX,
            String mapY,
            String radius,
            String contentTypeId,
            String keyword
    )
            throws IOException, InterruptedException {
        StringBuilder url = new StringBuilder(TOUR_API_BASE)
                .append("/locationBasedList2?")
                .append(baseQuery());
        if (notBlank(mapX)) url.append("&mapX=").append(urlEncode(mapX));
        if (notBlank(mapY)) url.append("&mapY=").append(urlEncode(mapY));
        if (notBlank(radius)) url.append("&radius=").append(urlEncode(radius));
        if (notBlank(contentTypeId)) url.append("&contentTypeId=").append(urlEncode(contentTypeId));
        if (notBlank(keyword)) url.append("&keyword=").append(urlEncode(keyword));
        return fetch(url.toString());
    }

    private List<Attraction> callAreaBasedList(String areaCode, String sigunguCode, String contentTypeId)
            throws IOException, InterruptedException {
        StringBuilder url = new StringBuilder(TOUR_API_BASE)
                .append("/areaBasedList2?")
                .append(baseQuery());
        if (notBlank(areaCode)) url.append("&areaCode=").append(urlEncode(areaCode));
        if (notBlank(sigunguCode)) url.append("&sigunguCode=").append(urlEncode(sigunguCode));
        if (notBlank(contentTypeId)) url.append("&contentTypeId=").append(urlEncode(contentTypeId));
        return fetch(url.toString());
    }

    private List<Attraction> callKeywordSearch(
            String keyword,
            String areaCode,
            String sigunguCode,
            String contentTypeId
    )
            throws IOException, InterruptedException {
        StringBuilder url = new StringBuilder(TOUR_API_BASE)
                .append("/searchKeyword2?")
                .append(baseQuery());
        if (notBlank(keyword)) url.append("&keyword=").append(urlEncode(keyword));
        if (notBlank(areaCode)) url.append("&areaCode=").append(urlEncode(areaCode));
        if (notBlank(sigunguCode)) url.append("&sigunguCode=").append(urlEncode(sigunguCode));
        if (notBlank(contentTypeId)) url.append("&contentTypeId=").append(urlEncode(contentTypeId));
        return fetch(url.toString());
    }

    private String baseQuery() {
        if (!notBlank(serviceKey)) {
            throw new IllegalStateException(
                    "Tour API key is missing. Set enjoytrip.external.tour-api.service-key."
            );
        }
        return "serviceKey=" + serviceKey
                + "&numOfRows=" + DEFAULT_ROWS
                + "&pageNo=1"
                + "&MobileOS=ETC"
                + "&MobileApp=EnjoyTrip"
                + "&arrange=Q"
                + "&_type=xml";
    }

    private List<Attraction> fetch(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Tour API HTTP error: " + response.statusCode());
        }
        return parseItems(response.body());
    }

    private List<Attraction> parseItems(String xml) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document document = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            NodeList itemNodes = document.getElementsByTagName("item");
            List<Attraction> rows = new ArrayList<>();
            for (int i = 0; i < itemNodes.getLength(); i++) {
                Element item = (Element) itemNodes.item(i);
                rows.add(new Attraction(
                        parseLong(text(item, "contentid")),
                        text(item, "title"),
                        text(item, "addr1"),
                        text(item, "addr2"),
                        text(item, "zipcode"),
                        text(item, "tel"),
                        text(item, "firstimage"),
                        text(item, "firstimage2"),
                        parseInt(text(item, "readcount")),
                        parseInt(text(item, "areacode")),
                        parseInt(text(item, "sigungucode")),
                        parseDouble(text(item, "mapy")),
                        parseDouble(text(item, "mapx")),
                        text(item, "mlevel"),
                        text(item, "contenttypeid"),
                        "",
                        0,
                        0.0,
                        0,
                        List.of(),
                        false,
                        null
                ));
            }
            return rows;
        } catch (Exception ex) {
            throw new IOException("Failed to parse Tour API response", ex);
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

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static Long parseLong(String value) {
        try {
            if (!notBlank(value)) {
                return null;
            }
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Integer parseInt(String value) {
        try {
            if (!notBlank(value)) {
                return 0;
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static Double parseDouble(String value) {
        try {
            if (!notBlank(value)) {
                return 0.0;
            }
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    public boolean isConfigured() {
        return notBlank(serviceKey);
    }
}

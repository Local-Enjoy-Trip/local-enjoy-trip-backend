package com.ssafy.enjoytrip.external;

import com.ssafy.enjoytrip.domain.NewsItem;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class NewsClient {
    private static final List<String> FEEDS = List.of(
            "https://news.google.com/rss/search?q=%EA%B4%80%EA%B4%91+%EC%97%AC%ED%96%89"
                    + "&hl=ko&gl=KR&ceid=KR:ko",
            "https://www.mk.co.kr/rss/50300009/"
    );

    private final HttpClient httpClient;

    public NewsClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public List<NewsItem> findNews() {
        for (String feed : FEEDS) {
            List<NewsItem> parsed = fetchAndParse(feed);
            if (!parsed.isEmpty()) {
                return parsed;
            }
        }
        return fallbackNews();
    }

    private List<NewsItem> fetchAndParse(String feedUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(feedUrl))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return List.of();
            }
            return parseRss(response.body());
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private List<NewsItem> parseRss(String xmlText) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xmlText.getBytes(StandardCharsets.UTF_8)));
            NodeList items = document.getElementsByTagName("item");
            List<NewsItem> results = new ArrayList<>();
            int limit = Math.min(items.getLength(), 20);
            for (int i = 0; i < limit; i++) {
                org.w3c.dom.Node item = items.item(i);
                String title = text(item, "title");
                String link = text(item, "link");
                String pubDate = text(item, "pubDate");
                String description = stripHtml(text(item, "description"));
                if (description.length() > 150) {
                    description = description.substring(0, 150) + "...";
                }
                results.add(new NewsItem(
                        "news_" + Instant.now().toEpochMilli() + "_" + i,
                        defaultTitle(title),
                        link,
                        defaultDescription(description),
                        "관광 뉴스",
                        defaultPubDate(pubDate)
                ));
            }
            return results;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private String text(org.w3c.dom.Node parent, String tagName) {
        if (!(parent instanceof org.w3c.dom.Element element)) {
            return "";
        }
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() == 0 || nodes.item(0) == null) {
            return "";
        }
        return String.valueOf(nodes.item(0).getTextContent()).trim();
    }

    private String stripHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
    }

    private String defaultTitle(String title) {
        if (title.isBlank()) {
            return "제목 없음";
        }
        return title;
    }

    private String defaultDescription(String description) {
        if (description.isBlank()) {
            return "요약 정보 없음";
        }
        return description;
    }

    private String defaultPubDate(String pubDate) {
        if (pubDate.isBlank()) {
            return Instant.now().toString();
        }
        return pubDate;
    }

    private List<NewsItem> fallbackNews() {
        String now = Instant.now().toString();
        return List.of(
                new NewsItem("fallback_news_1", "봄 시즌 국내 여행지 추천", "https://korean.visitkorea.or.kr",
                        "봄철 가볼 만한 국내 관광지 정보를 모아 여행 계획에 참고할 수 있습니다.", "대한민국 구석구석", now),
                new NewsItem("fallback_news_2", "지역 축제와 함께하는 주말 여행", "https://korean.visitkorea.or.kr",
                        "전국 지역 축제를 중심으로 당일치기와 1박2일 코스를 구성해보세요.", "대한민국 구석구석", now)
        );
    }
}

package com.ssafy.enjoytrip.external;

import com.ssafy.enjoytrip.external.ExternalClientTestSupport.FakeHttpClient;
import com.ssafy.enjoytrip.domain.NewsItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NewsClientTest {
    @Test
    void findNewsUsesFirstSuccessfulFeedAndMapsRssItems() {
        FakeHttpClient http = new FakeHttpClient()
                .enqueue(502, "bad gateway")
                .enqueue(200, """
                        <rss><channel>
                          <item>
                            <title>제주 여행 뉴스</title>
                            <link>https://example.test/news/1</link>
                            <pubDate>Fri, 15 May 2026 09:00:00 +0900</pubDate>
                            <description><![CDATA[<p>제주 <b>관광</b> 소식입니다.</p>]]></description>
                          </item>
                          <item>
                            <link>https://example.test/news/2</link>
                            <description></description>
                          </item>
                        </channel></rss>
                        """);
        NewsClient client = new NewsClient(http);

        List<NewsItem> results = client.findNews();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).title()).isEqualTo("제주 여행 뉴스");
        assertThat(results.get(0).link()).isEqualTo("https://example.test/news/1");
        assertThat(results.get(0).summary()).isEqualTo("제주 관광 소식입니다.");
        assertThat(results.get(0).source()).isEqualTo("관광 뉴스");
        assertThat(results.get(0).publishedAt()).isEqualTo("Fri, 15 May 2026 09:00:00 +0900");
        assertThat(results.get(1).title()).isEqualTo("제목 없음");
        assertThat(results.get(1).summary()).isEqualTo("요약 정보 없음");
        assertThat(http.requests()).hasSize(2);
    }

    @Test
    void findNewsLimitsToTwentyItemsAndTruncatesLongDescriptions() {
        StringBuilder rss = new StringBuilder("<rss><channel>");
        for (int i = 0; i < 25; i++) {
            rss.append("<item><title>뉴스 ")
                    .append(i)
                    .append("</title><link>https://example.test/")
                    .append(i)
                    .append("</link><description>")
                    .append("가".repeat(160))
                    .append("</description></item>");
        }
        rss.append("</channel></rss>");
        NewsClient client = new NewsClient(new FakeHttpClient().enqueue(200, rss.toString()));

        List<NewsItem> results = client.findNews();

        assertThat(results).hasSize(20);
        assertThat(results.getFirst().summary()).hasSize(153).endsWith("...");
    }

    @Test
    void findNewsFallsBackWhenFeedsFailOrXmlIsMalformed() {
        FakeHttpClient http = new FakeHttpClient()
                .enqueueIOException("offline")
                .enqueue(200, "<rss><item>");
        NewsClient client = new NewsClient(http);

        List<NewsItem> results = client.findNews();

        assertThat(results)
                .extracting(NewsItem::id)
                .containsExactly("fallback_news_1", "fallback_news_2");
        assertThat(results)
                .extracting(NewsItem::source)
                .containsExactly("대한민국 구석구석", "대한민국 구석구석");
        assertThat(http.requests()).hasSize(2);
    }
}

package com.ssafy.enjoytrip.storage.repository;

import static com.ssafy.enjoytrip.storage.jooq.tables.NewsItems.NEWS_ITEMS;

import com.ssafy.enjoytrip.domain.NewsItem;
import com.ssafy.enjoytrip.repository.NewsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NewsStorageRepository implements NewsRepository {
    private final DSLContext dsl;

    @Override
    public List<NewsItem> findNews() {
        return dsl.select(
                        NEWS_ITEMS.ID,
                        NEWS_ITEMS.TITLE,
                        NEWS_ITEMS.LINK,
                        NEWS_ITEMS.SUMMARY,
                        NEWS_ITEMS.SOURCE,
                        NEWS_ITEMS.PUBLISHED_AT
                )
                .from(NEWS_ITEMS)
                .orderBy(NEWS_ITEMS.CREATED_AT.desc(), NEWS_ITEMS.ID.desc())
                .limit(20)
                .fetchInto(NewsItem.class);
    }
}

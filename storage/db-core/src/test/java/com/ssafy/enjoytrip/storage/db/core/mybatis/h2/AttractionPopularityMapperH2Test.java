package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AttractionPopularityMapperH2Test extends H2MapperTestSupport {
    @Autowired
    private AttractionMapper attractionMapper;

    @BeforeEach
    void prepareAttractionPopularityTables() {
        jdbcTemplate.execute("""
                create table if not exists attractions (
                    id bigint primary key,
                    title varchar(255)
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists attraction_favorites (
                    attraction_id bigint not null,
                    user_id varchar(64) not null,
                    primary key (attraction_id, user_id)
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists attraction_popularity_stats (
                    attraction_id bigint primary key,
                    favorite_count integer not null default 0,
                    popularity_score numeric(12, 4) not null default 0,
                    updated_at timestamp not null default current_timestamp
                )
                """);
        jdbcTemplate.update("delete from attraction_popularity_stats");
        jdbcTemplate.update("delete from attraction_favorites");
        jdbcTemplate.update("delete from attractions");
    }

    @DisplayName("AttractionMapper는 popularity favorite_count 델타를 0 미만으로 낮추지 않는다")
    @Test
    void incrementPopularityFavoriteCountClampsAtZero() {
        seedAttraction(1L);

        attractionMapper.incrementPopularityFavoriteCount(1L, 2);
        attractionMapper.incrementPopularityFavoriteCount(1L, -1);
        attractionMapper.incrementPopularityFavoriteCount(1L, -5);

        Integer favoriteCount = jdbcTemplate.queryForObject(
                "select favorite_count from attraction_popularity_stats where attraction_id = 1",
                Integer.class
        );
        Integer popularityScore = jdbcTemplate.queryForObject(
                "select popularity_score from attraction_popularity_stats where attraction_id = 1",
                Integer.class
        );

        assertThat(favoriteCount).isZero();
        assertThat(popularityScore).isZero();
    }

    @DisplayName("AttractionMapper는 attraction_favorites를 기준으로 popularity favorite_count를 재동기화한다")
    @Test
    void reconcilePopularityFavoriteCountsFromFavorites() {
        seedAttraction(1L);
        seedAttraction(2L);
        jdbcTemplate.update("""
                insert into attraction_popularity_stats (attraction_id, favorite_count, popularity_score)
                values (1, 9, 9), (2, 4, 4)
                """);
        jdbcTemplate.update("""
                insert into attraction_favorites (attraction_id, user_id)
                values (1, 'user-a'), (1, 'user-b')
                """);

        attractionMapper.reconcilePopularityFavoriteCounts();

        Integer firstCount = jdbcTemplate.queryForObject(
                "select favorite_count from attraction_popularity_stats where attraction_id = 1",
                Integer.class
        );
        Integer secondCount = jdbcTemplate.queryForObject(
                "select favorite_count from attraction_popularity_stats where attraction_id = 2",
                Integer.class
        );

        assertThat(firstCount).isEqualTo(2);
        assertThat(secondCount).isZero();
    }

    private void seedAttraction(Long attractionId) {
        jdbcTemplate.update(
                "insert into attractions (id, title) values (?, ?)",
                attractionId,
                "attraction-" + attractionId
        );
    }
}

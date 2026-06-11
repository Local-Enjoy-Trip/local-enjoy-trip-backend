package com.ssafy.enjoytrip.external;

import com.ssafy.enjoytrip.repository.AttractionPopularityRepository;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickHouseAttractionPopularityRepository implements AttractionPopularityRepository {
    private final ClickHousePopularityProperties properties;

    @Override
    public Map<Long, Long> findFavoriteCounts(Collection<Long> attractionIds) {
        List<Long> normalizedIds = attractionIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        if (normalizedIds.isEmpty()) {
            return Map.of();
        }

        try (Connection connection = DriverManager.getConnection(
                properties.getUrl(),
                properties.getUsername(),
                properties.getPassword()
        ); PreparedStatement statement = connection.prepareStatement(query(normalizedIds.size()))) {
            statement.setQueryTimeout((int) properties.getQueryTimeout().toSeconds());

            for (int index = 0; index < normalizedIds.size(); index++) {
                statement.setLong(index + 1, normalizedIds.get(index));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                Map<Long, Long> counts = new LinkedHashMap<>();

                while (resultSet.next()) {
                    counts.put(resultSet.getLong("attraction_id"), resultSet.getLong("favorite_count"));
                }

                return counts;
            }
        } catch (SQLException | RuntimeException exception) {
            log.warn(
                    "ClickHouse attraction popularity lookup failed. candidateCount={}, exceptionType={}",
                    normalizedIds.size(),
                    exception.getClass().getSimpleName(),
                    exception
            );

            return Map.of();
        }
    }

    private static String query(int size) {
        StringJoiner placeholders = new StringJoiner(", ");

        for (int index = 0; index < size; index++) {
            placeholders.add("?");
        }

        return """
                SELECT attraction_id, sum(favorite_count) AS favorite_count
                FROM attraction_favorites_counts
                WHERE attraction_id IN (%s)
                GROUP BY attraction_id
                """.formatted(placeholders);
    }
}

package com.ssafy.enjoytrip.storage.repository;

import com.ssafy.enjoytrip.repository.DbHealthRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Repository
public class DbHealthStorageRepository implements DbHealthRepository {
    private final DataSource dataSource;

    public DbHealthStorageRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean isConnected() {
        try (Connection ignored = dataSource.getConnection()) {
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }
}

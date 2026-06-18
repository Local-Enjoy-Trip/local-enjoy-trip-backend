package com.ssafy.enjoytrip.core.domain.service;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;

@Service
public class DbHealthService {
    private final DataSource dataSource;

    public DbHealthService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isConnected() {
        try (Connection ignored = dataSource.getConnection()) {
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }
}

package com.ssafy.enjoytrip.external;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "enjoytrip.external.clickhouse")
public class ClickHousePopularityProperties {
    private String url = "jdbc:clickhouse://localhost:8123/default";
    private String username = "default";
    private String password = "enjoytrip_clickhouse";
    private Duration queryTimeout = Duration.ofSeconds(3);

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Duration getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(Duration queryTimeout) {
        this.queryTimeout = queryTimeout;
    }
}

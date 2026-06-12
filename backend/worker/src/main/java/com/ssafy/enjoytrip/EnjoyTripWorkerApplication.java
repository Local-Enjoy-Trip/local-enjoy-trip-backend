package com.ssafy.enjoytrip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
        basePackages = "com.ssafy.enjoytrip",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.ssafy\\.enjoytrip\\.service\\.(?!NotificationOutboxProcessor$).*"
        )
)
public class EnjoyTripWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnjoyTripWorkerApplication.class, args);
    }
}

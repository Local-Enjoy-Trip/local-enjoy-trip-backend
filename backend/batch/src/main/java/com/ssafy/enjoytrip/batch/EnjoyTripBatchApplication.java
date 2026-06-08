package com.ssafy.enjoytrip.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.ssafy.enjoytrip.batch",
        "com.ssafy.enjoytrip.service.embedding",
        "com.ssafy.enjoytrip.external"
})
public class EnjoyTripBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnjoyTripBatchApplication.class, args);
    }
}

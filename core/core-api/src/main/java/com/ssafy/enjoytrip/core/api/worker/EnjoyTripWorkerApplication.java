package com.ssafy.enjoytrip.core.api.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ssafy.enjoytrip")
public class EnjoyTripWorkerApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(EnjoyTripWorkerApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }
}

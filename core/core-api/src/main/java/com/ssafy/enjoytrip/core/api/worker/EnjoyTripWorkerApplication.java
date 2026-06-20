package com.ssafy.enjoytrip.core.api.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(WorkerConfiguration.class)
public class EnjoyTripWorkerApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(EnjoyTripWorkerApplication.class);
        application.setAdditionalProfiles("worker");
        application.run(args);
    }
}

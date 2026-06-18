package com.ssafy.enjoytrip.core.api.worker;

import com.ssafy.enjoytrip.core.api.config.CoreApiStorageConfiguration;
import com.ssafy.enjoytrip.core.domain.service.NotificationOutboxProcessor;
import com.ssafy.enjoytrip.core.domain.service.NotificationService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        CoreApiStorageConfiguration.class,
        NotificationService.class,
        NotificationOutboxProcessor.class
})
public class NotificationWorkerConfiguration {
}

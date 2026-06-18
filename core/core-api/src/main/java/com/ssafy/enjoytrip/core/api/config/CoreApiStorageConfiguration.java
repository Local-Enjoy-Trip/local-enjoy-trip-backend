package com.ssafy.enjoytrip.core.api.config;

import com.ssafy.enjoytrip.storage.db.core.StorageConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(StorageConfiguration.class)
public class CoreApiStorageConfiguration {
}

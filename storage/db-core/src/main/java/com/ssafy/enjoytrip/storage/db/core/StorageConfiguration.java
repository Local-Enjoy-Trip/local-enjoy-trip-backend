package com.ssafy.enjoytrip.storage.db.core;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan(basePackages = "com.ssafy.enjoytrip.storage.db.core")
@EnableJpaAuditing(modifyOnCreate = false)
@EnableJpaRepositories(basePackages = "com.ssafy.enjoytrip.storage.db.core.jpa")
@EntityScan(basePackages = "com.ssafy.enjoytrip.storage.db.core.entity")
public class StorageConfiguration {
}

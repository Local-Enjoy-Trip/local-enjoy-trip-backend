package com.ssafy.enjoytrip.storage;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan(basePackages = "com.ssafy.enjoytrip.storage")
@EnableJpaAuditing(modifyOnCreate = false)
@EnableJpaRepositories(basePackages = "com.ssafy.enjoytrip.storage.jpa")
@EntityScan(basePackages = "com.ssafy.enjoytrip.storage.entity")
public class StorageConfiguration {
}

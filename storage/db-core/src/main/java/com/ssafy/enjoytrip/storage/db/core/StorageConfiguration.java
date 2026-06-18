package com.ssafy.enjoytrip.storage.db.core;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = "com.ssafy.enjoytrip.storage.db.core")
@MapperScan(basePackages = "com.ssafy.enjoytrip.storage.db.core.mybatis.mapper")
public class StorageConfiguration {
}

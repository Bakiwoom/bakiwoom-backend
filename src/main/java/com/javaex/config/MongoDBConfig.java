package com.javaex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.javaex.idea.repository.mongo")
public class MongoDBConfig {
    // MongoDB 설정은 application.properties에서 자동으로 로드됩니다.
} 
package com.javaex.idea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.javaex")
@EnableMongoRepositories
@EnableScheduling
public class IdeaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdeaBackendApplication.class, args);
    }

}

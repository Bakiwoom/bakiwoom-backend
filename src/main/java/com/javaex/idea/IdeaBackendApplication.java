package com.javaex.idea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.javaex")
public class IdeaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdeaBackendApplication.class, args);
    }

}

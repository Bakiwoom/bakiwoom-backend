package com.javaex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class ApiKeyConfig {
    
    @Value("${api.service-key.encoded}")
    private String encodedKey;
    
    @Value("${api.service-key.decoded}")
    private String decodedKey;
    
    public String getKey(boolean useEncoded) {
        return useEncoded ? encodedKey : decodedKey;
    }
} 
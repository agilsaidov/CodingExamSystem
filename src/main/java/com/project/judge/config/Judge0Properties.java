package com.project.judge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "judge0")
@Data
public class Judge0Properties {

    private String url;
    private String apiKey;
    private Integer maxRetries = 3;
    private Integer retryDelayMs = 1000;
    private Integer connectionTimeout = 10000; // ms
    private Integer readTimeout = 30000; // ms
    
    // Default limits
    private Double defaultCpuTimeLimit = 5.0; // seconds
    private Integer defaultMemoryLimit = 128000; // KB
    private Double defaultWallTimeLimit = 10.0; // seconds
}
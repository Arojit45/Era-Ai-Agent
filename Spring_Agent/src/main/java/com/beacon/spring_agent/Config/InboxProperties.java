package com.beacon.spring_agent.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "support-agent.inbox")
public record InboxProperties(
        String baseUrl,
        String address,
        long pollInterval,
        int batchSize
) {
    public InboxProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8025";
        }
        if (address == null || address.isBlank()) {
            address = "support@example.com";
        }
        if (batchSize <= 0) {
            batchSize = 50;
        }
    }
}

package com.dealflow.backend.cpq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cpq")
public record CpqConfiguration(
        boolean enabled,
        String baseUrl,
        String username,
        String password
) {
}

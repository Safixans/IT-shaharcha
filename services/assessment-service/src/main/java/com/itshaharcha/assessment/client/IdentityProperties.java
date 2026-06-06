package com.itshaharcha.assessment.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Where to reach identity-service for grading-authorization (teacher↔student) checks. */
@ConfigurationProperties(prefix = "app.identity")
public record IdentityProperties(String baseUrl) {

    public IdentityProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:9001";
        }
    }
}

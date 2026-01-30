package com.hcmus.course_recommendation.auth.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "domain")
public record DomainProperties(
	String frontendUrl
) {
}

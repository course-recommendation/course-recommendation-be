package com.hcmus.course_recommendation.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

import com.hcmus.course_recommendation.auth.properties.DomainProperties;
import com.hcmus.course_recommendation.recommendation.fs.client.FSClient;

import lombok.RequiredArgsConstructor;

@Configuration
@ImportHttpServices(types = {FSClient.class})
@RequiredArgsConstructor
public class RecommenderClientConfig {

	private final DomainProperties domainProperties;

	@Bean
	RestClientHttpServiceGroupConfigurer groupConfigurer() {
		return groups -> {
			groups.forEachClient((group, builder) -> builder
				.baseUrl(domainProperties.aiUrl())
				.build());
		};
	}
}

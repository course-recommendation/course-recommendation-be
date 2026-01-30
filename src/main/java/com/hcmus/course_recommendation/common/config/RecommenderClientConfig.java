package com.hcmus.course_recommendation.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

import com.hcmus.course_recommendation.recommendation.fs.client.FSClient;

@Configuration
@ImportHttpServices(types = {FSClient.class})
public class RecommenderClientConfig {

	@Bean
	RestClientHttpServiceGroupConfigurer groupConfigurer() {
		return groups -> {
			groups.forEachClient((group, builder) -> builder
				.baseUrl("http://localhost:8000")
				.build());
		};
	}
}

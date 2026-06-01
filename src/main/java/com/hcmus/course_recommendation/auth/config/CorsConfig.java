package com.hcmus.course_recommendation.auth.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.hcmus.course_recommendation.auth.properties.DomainProperties;

@Configuration
public class CorsConfig {
	@Bean
	public CorsConfigurationSource corsConfigurationSource(DomainProperties domainProperties) {
		CorsConfiguration configuration = new CorsConfiguration();
		var allowedOrigins = Arrays.stream(domainProperties.frontendUrl().split(","))
			.map(String::trim)
			.filter(origin -> !origin.isBlank())
			.toList();
		configuration.setAllowedOriginPatterns(allowedOrigins);
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowedMethods(List.of("*"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}

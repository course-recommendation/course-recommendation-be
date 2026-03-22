package com.hcmus.course_recommendation.recommendation.fs.model;

import java.util.Map;

import lombok.Builder;

@Builder
public record UserPreferenceData(
	Map<String, Double> attributeToScore
) {
}

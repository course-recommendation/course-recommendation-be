package com.hcmus.course_recommendation.recommendation.tri_rank.client.dto;

import java.util.List;

import lombok.Builder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ClientTriRankRecommendationResult(
	List<String> recommendations
) {
}


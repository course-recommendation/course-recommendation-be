package com.hcmus.course_recommendation.recommendation.tri_rank.client.dto;

import lombok.Builder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ClientTriRankTrainRequest(
	Long tenantId
) {
}

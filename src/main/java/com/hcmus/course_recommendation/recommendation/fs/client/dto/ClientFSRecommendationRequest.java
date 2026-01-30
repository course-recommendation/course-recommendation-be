package com.hcmus.course_recommendation.recommendation.fs.client.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ClientFSRecommendationRequest(
	List<String> attributes,
	Map<String, List<ClientFSItemSentiment>> itemIdToItemSentiments,
	Map<String, ClientFSPreferenceConfigure> attributeToPreferenceConfigure
) {
}

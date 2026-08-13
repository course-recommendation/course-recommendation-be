package com.hcmus.course_recommendation.recommendation.fs.client.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ClientFSRefinedRecommendationRequest(
	List<String> attributes,
	Map<String, List<ClientFSItemSentiment>> itemIdToItemSentiments,
	// The item the user endorsed; the recommender retargets onto its sentiments.
	String itemId,
	List<ClientFSTradeoffPair> itemTradeoffVector,
	List<ClientFSTradeoffPair> category,
	Map<String, ClientFSPreferenceConfigure> oldAttributeToPreferenceConfigure
) {
}

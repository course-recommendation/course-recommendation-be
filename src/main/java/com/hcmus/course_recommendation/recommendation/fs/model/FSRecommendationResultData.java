package com.hcmus.course_recommendation.recommendation.fs.model;

import java.util.List;
import java.util.Map;

import lombok.Builder;

@Builder(toBuilder = true)
public record FSRecommendationResultData(
	Map<String, FSPreferenceConfigure> attributeToPreferenceConfigure,
	String topItemId,
	List<FSCategoryDetail> categoryDetails,
	Map<String, List<FSTradeoffPair>> itemIdToTradeoffVector
) {
}

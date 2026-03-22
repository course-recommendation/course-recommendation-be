package com.hcmus.course_recommendation.recommendation.fs.model;

import java.util.List;
import java.util.Map;

import com.hcmus.course_recommendation.course.model.Algorithm;

import lombok.Builder;

@Builder(toBuilder = true)
public record FsRecommendationResultData(
	Map<String, FSPreferenceConfigure> attributeToPreferenceConfigure,
	String topItemId,
	List<FSCategoryDetail> categoryDetails,
	Map<String, List<FSTradeoffPair>> itemIdToTradeoffVector
) implements RecommendationResultData {
	@Override
	public Algorithm getAlgorithm() {
		return Algorithm.FS;
	}
}

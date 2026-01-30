package com.hcmus.course_recommendation.recommendation.fs.model;

import java.util.List;

public record FSCategoryDetail(
	List<FSTradeoffPair> category,
	List<String> itemIds
) {
}

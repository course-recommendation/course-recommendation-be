package com.hcmus.course_recommendation.course.model;

import lombok.Builder;

@Builder
public record FSItemSentiment(
	String attribute,
	Double sentimentScore
) {
}

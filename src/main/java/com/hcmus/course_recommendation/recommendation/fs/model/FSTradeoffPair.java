package com.hcmus.course_recommendation.recommendation.fs.model;

import lombok.Builder;

@Builder
public record FSTradeoffPair(
	String attribute,
	FSTradeoffDirection direction
) {
}

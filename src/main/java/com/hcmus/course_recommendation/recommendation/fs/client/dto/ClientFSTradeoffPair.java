package com.hcmus.course_recommendation.recommendation.fs.client.dto;

import lombok.Builder;

@Builder
public record ClientFSTradeoffPair(
	String attribute,
	ClientFSTradeoffDirection direction
) {
}

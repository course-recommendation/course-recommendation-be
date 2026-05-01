package com.hcmus.course_recommendation.recommendation.tri_rank.model;

import lombok.Builder;

@Builder
public record TriRankItemAspect(
	String aspect,
	Double score
) {
}


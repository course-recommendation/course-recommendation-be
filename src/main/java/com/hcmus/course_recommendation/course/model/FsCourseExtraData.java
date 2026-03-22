package com.hcmus.course_recommendation.course.model;

import java.util.List;

import lombok.Builder;

@Builder
public record FsCourseExtraData(
	List<FSItemSentiment> itemSentiments
) implements CourseExtraData {
	@Override
	public Algorithm getAlgorithm() {
		return Algorithm.FS;
	}
}

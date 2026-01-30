package com.hcmus.course_recommendation.course.model;

import java.util.List;

import lombok.Builder;

@Builder
public record FSCourseExtraData(
	List<FSItemSentiment> itemSentiments
) implements CourseExtraData {
	@Override
	public CourseAlgorithm getAlgorithm() {
		return CourseAlgorithm.FS;
	}
}

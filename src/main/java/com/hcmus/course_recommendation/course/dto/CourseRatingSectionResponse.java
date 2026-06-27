package com.hcmus.course_recommendation.course.dto;

import java.util.List;

public record CourseRatingSectionResponse(
	Long courseId,
	String courseCode,
	String courseName,
	List<CourseAttributeRatingResponse> attributeRatings
) {
	public record CourseAttributeRatingResponse(
		Long attributeId,
		String attributeName,
		Integer score
	) {}
}

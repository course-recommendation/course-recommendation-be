package com.hcmus.course_recommendation.course.dto;

import java.util.List;

public record CourseRatingSectionResponse(
	Long courseId,
	String courseCode,
	String courseName,
	/** Overall satisfaction, rounded to whole stars; null if the user has not given one. */
	Integer satisfaction,
	List<CourseAttributeRatingResponse> attributeRatings
) {
	public record CourseAttributeRatingResponse(
		Long attributeId,
		String attributeName,
		Integer score
	) {}
}

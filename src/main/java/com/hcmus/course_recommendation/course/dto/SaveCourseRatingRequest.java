package com.hcmus.course_recommendation.course.dto;

import java.util.List;

public record SaveCourseRatingRequest(List<Section> sections) {
	/**
	 * @param satisfaction overall 1-5 star rating of the course; null leaves any existing score
	 *                     untouched, 0 clears it (see {@code CourseService.rateCourseSatisfaction})
	 */
	public record Section(Long courseId, Integer satisfaction, List<AttributeRating> attributeRatings) {}

	public record AttributeRating(Long attributeId, Integer score) {}
}

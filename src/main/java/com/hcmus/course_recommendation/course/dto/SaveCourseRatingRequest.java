package com.hcmus.course_recommendation.course.dto;

import java.util.List;

public record SaveCourseRatingRequest(List<Section> sections) {
	public record Section(Long courseId, List<AttributeRating> attributeRatings) {}
	public record AttributeRating(Long attributeId, Integer score) {}
}

package com.hcmus.course_recommendation.recommendation.admin.dto;

public record AdminRatingRow(Long id, String userId, Long courseId, String courseCode, Long attributeId,
	String attributeName, Integer score) {
}

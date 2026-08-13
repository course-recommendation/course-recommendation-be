package com.hcmus.course_recommendation.recommendation.admin.dto;

import java.time.LocalDateTime;

import com.hcmus.course_recommendation.course.model.Algorithm;

public record AdminAttributeRow(Long id, String value, String lowLabel, String highLabel, Algorithm algorithm,
	LocalDateTime createdAt) {
}

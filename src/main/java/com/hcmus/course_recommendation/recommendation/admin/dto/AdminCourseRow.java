package com.hcmus.course_recommendation.recommendation.admin.dto;

import java.time.LocalDateTime;

import com.hcmus.course_recommendation.course.model.Algorithm;

public record AdminCourseRow(Long id, String code, String name, String description, Algorithm algorithm,
	LocalDateTime createdAt) {
}

package com.hcmus.course_recommendation.recommendation.admin.dto;

import com.hcmus.course_recommendation.course.model.Algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpsertCourseRequest {
	private String code;
	private String name;
	private String description;
	private String thumbnailUrl;
	private Algorithm algorithm;
}

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
public class UpsertAttributeRequest {
	private String value;
	private Algorithm algorithm;
}

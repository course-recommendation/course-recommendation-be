package com.hcmus.course_recommendation.recommendation.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRatingRequest {
	private String userId;
	private Long courseId;
	private Long attributeId;
	private Integer score;
}

package com.hcmus.course_recommendation.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RateCourseRequest {
	private String attributeValue;
	private Integer score;
}

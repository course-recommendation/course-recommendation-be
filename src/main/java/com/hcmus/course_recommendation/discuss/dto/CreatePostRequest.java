package com.hcmus.course_recommendation.discuss.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hcmus.course_recommendation.course.model.Algorithm;

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
public class CreatePostRequest {
	@JsonIgnore
	private String userId;
	@JsonIgnore
	private Long tenantId;
	private Algorithm algorithm;
	private String content;
	private String courseCode;
}

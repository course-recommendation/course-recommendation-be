package com.hcmus.course_recommendation.recommendation.fs.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hcmus.course_recommendation.course.model.Dataset;
import com.hcmus.course_recommendation.recommendation.fs.model.FSPreferenceConfigure;

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
public class FSRecommendationRequest {
	private Dataset dataset;
	private Map<String, FSPreferenceConfigure> attributeToPreferenceConfigure;
	@JsonIgnore
	private String userId;
}

package com.hcmus.course_recommendation.recommendation.tri_rank.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hcmus.course_recommendation.recommendation.model.FilterCoursesOption;

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
public class TriRankRecommendationRequest {
	private Map<String, Double> attributeToScore;
	private List<FilterCoursesOption> filterCoursesOptions;
	private List<String> customFilteredCourseCodes;
	@JsonIgnore
	private String userId;
	@JsonIgnore
	private Long tenantId;
}


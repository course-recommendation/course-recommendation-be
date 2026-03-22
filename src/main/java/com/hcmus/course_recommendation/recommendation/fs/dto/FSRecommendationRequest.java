package com.hcmus.course_recommendation.recommendation.fs.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hcmus.course_recommendation.course.model.Dataset;
import com.hcmus.course_recommendation.recommendation.fs.model.FSPreferenceConfigure;
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
public class FSRecommendationRequest {
	private Dataset dataset;
	private Map<String, FSPreferenceConfigure> attributeToPreferenceConfigure;
	private List<FilterCoursesOption> filterCoursesOptions;
	private List<String> customFilteredCourseCodes;
	@JsonIgnore
	private String userId;
}

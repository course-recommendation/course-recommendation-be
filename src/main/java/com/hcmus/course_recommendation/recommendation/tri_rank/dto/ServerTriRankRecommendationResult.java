package com.hcmus.course_recommendation.recommendation.tri_rank.dto;

import java.util.List;
import java.util.Map;

import com.hcmus.course_recommendation.course.dto.CourseDetail;
import com.hcmus.course_recommendation.recommendation.model.FilterCoursesOption;
import com.hcmus.course_recommendation.recommendation.tri_rank.model.TriRankItemAspect;

import lombok.Builder;

@Builder
public record ServerTriRankRecommendationResult(
	Long id,
	List<CourseDetail> courseDetails,
	List<FilterCoursesOption> filterCoursesOptions,
	List<String> customFilteredCourseCodes,
	Map<String, List<TriRankItemAspect>> itemIdToItemAspects
) {
}



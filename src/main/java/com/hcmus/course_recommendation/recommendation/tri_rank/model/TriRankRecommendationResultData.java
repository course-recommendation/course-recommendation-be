package com.hcmus.course_recommendation.recommendation.tri_rank.model;

import java.util.List;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.recommendation.fs.model.RecommendationResultData;
import com.hcmus.course_recommendation.recommendation.model.FilterCoursesOption;

import lombok.Builder;

@Builder(toBuilder = true)
public record TriRankRecommendationResultData(
	List<String> courseCodes,
	List<FilterCoursesOption> filterCoursesOptions,
	List<String> customFilteredCourseCodes
) implements RecommendationResultData {
	@Override
	public Algorithm getAlgorithm() {
		return Algorithm.TRI_RANK;
	}
}



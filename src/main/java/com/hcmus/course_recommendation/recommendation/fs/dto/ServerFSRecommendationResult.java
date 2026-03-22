package com.hcmus.course_recommendation.recommendation.fs.dto;

import java.util.List;
import java.util.Map;

import com.hcmus.course_recommendation.course.dto.CourseDetail;
import com.hcmus.course_recommendation.recommendation.fs.model.FSPreferenceConfigure;
import com.hcmus.course_recommendation.recommendation.fs.model.FSTradeoffPair;
import com.hcmus.course_recommendation.recommendation.model.FilterCoursesOption;

import lombok.Builder;

@Builder
public record ServerFSRecommendationResult(
	Long id,
	Map<String, FSPreferenceConfigure> attributeToPreferenceConfigure,
	CourseDetail topCourseDetail,
	List<ServerFSCategoryDetail> categoryDetails,
	Map<String, List<FSTradeoffPair>> itemIdToTradeoffVector,
	List<FilterCoursesOption> filterCoursesOptions,
	List<String> customFilteredCourseCodes
) {
}

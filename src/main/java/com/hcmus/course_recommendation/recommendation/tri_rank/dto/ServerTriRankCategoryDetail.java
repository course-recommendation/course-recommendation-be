package com.hcmus.course_recommendation.recommendation.tri_rank.dto;

import java.util.List;

import com.hcmus.course_recommendation.course.dto.CourseDetail;
import com.hcmus.course_recommendation.recommendation.tri_rank.model.TriRankItemAspect;

import lombok.Builder;

@Builder
public record ServerTriRankCategoryDetail(
	List<TriRankItemAspect> category,
	List<CourseDetail> courseDetails
) {
}


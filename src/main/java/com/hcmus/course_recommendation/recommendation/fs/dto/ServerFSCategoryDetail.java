package com.hcmus.course_recommendation.recommendation.fs.dto;

import java.util.List;

import com.hcmus.course_recommendation.course.dto.CourseDetail;
import com.hcmus.course_recommendation.recommendation.fs.model.FSTradeoffPair;

import lombok.Builder;

@Builder
public record ServerFSCategoryDetail(
	List<FSTradeoffPair> category,
	List<CourseDetail> courseDetails
) {
}

package com.hcmus.course_recommendation.course.dto;

import java.util.Map;

import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.model.UserCourseStatusEnum;

import lombok.Builder;

@Builder(toBuilder = true)
public record CourseDetail(
	Course course,
	UserCourseStatusEnum userCourseStatus,
	Map<Long, Integer> userAttributeIdToRatingScore
) {
}

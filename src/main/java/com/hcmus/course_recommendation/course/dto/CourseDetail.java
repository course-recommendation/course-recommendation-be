package com.hcmus.course_recommendation.course.dto;

import java.util.List;

import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.model.UserCourseStatus;

import lombok.Builder;

@Builder(toBuilder = true)
public record CourseDetail(
	Course course,
	List<UserCourseStatus> userCourseStatuses
) {
}

package com.hcmus.course_recommendation.course.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hcmus.course_recommendation.course.model.UserCourseStatus;

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
public class GetCoursesOfUserRequest {
	private CourseDomain courseDomain;
	private UserCourseStatus userCourseStatus;
	@JsonIgnore
	private String userId;
}

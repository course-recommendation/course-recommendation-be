package com.hcmus.course_recommendation.course.dto;

import java.util.List;

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
public class UpdateUserCoursesRequest {
	@JsonIgnore
	private String userId;
	private UserCourseStatus userCourseStatus;
	private List<String> courseIds;
	private CourseDomain courseDomain;
}

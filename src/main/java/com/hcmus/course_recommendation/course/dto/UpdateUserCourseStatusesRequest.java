package com.hcmus.course_recommendation.course.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.UserCourseStatusEnum;

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
public class UpdateUserCourseStatusesRequest {
	@JsonIgnore
	private String userId;
	private UserCourseStatusEnum userCourseStatus;
	private List<Long> courseIds;
	private Algorithm algorithm;
}

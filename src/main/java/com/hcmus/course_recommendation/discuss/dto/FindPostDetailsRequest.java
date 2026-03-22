package com.hcmus.course_recommendation.discuss.dto;

import com.hcmus.course_recommendation.common.util.ListRequest;
import com.hcmus.course_recommendation.course.dto.Domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindPostDetailsRequest {
	private Domain domain;
	private ListRequest<String> courseIdsRequest = ListRequest.defaultInstance();
}

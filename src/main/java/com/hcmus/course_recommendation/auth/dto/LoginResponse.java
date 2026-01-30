package com.hcmus.course_recommendation.auth.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
	String accessToken
) {
}

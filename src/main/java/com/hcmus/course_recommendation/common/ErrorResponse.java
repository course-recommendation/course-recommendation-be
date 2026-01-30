package com.hcmus.course_recommendation.common;

import com.hcmus.course_recommendation.common.exception.GlobalErrorCode;

import lombok.Builder;

@Builder
public record ErrorResponse(boolean ok, GlobalErrorCode errorCode, Object errorData) {
	public static ErrorResponse create(GlobalErrorCode errorCode, Object errorData) {
		return ErrorResponse.builder().ok(false).errorCode(errorCode).errorData(errorData).build();
	}

	public static ErrorResponse create(GlobalErrorCode errorCode) {
		return create(errorCode, null);
	}
}

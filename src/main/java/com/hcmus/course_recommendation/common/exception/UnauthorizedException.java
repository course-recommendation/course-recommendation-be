package com.hcmus.course_recommendation.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApplicationRuntimeException {

	public UnauthorizedException(GlobalErrorCode code) {
		super(HttpStatus.UNAUTHORIZED, code);
	}

	public UnauthorizedException(GlobalErrorCode code, Object data) {
		super(HttpStatus.UNAUTHORIZED, code, data);
	}
}

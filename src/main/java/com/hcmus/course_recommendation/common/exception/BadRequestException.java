package com.hcmus.course_recommendation.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApplicationRuntimeException {

	public BadRequestException(GlobalErrorCode code) {
		super(HttpStatus.BAD_REQUEST, code);
	}

	public BadRequestException(GlobalErrorCode code, Object data) {
		super(HttpStatus.BAD_REQUEST, code, data);
	}
}

package com.hcmus.course_recommendation.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApplicationRuntimeException {

	public NotFoundException() {
		super(HttpStatus.NOT_FOUND, GlobalErrorCode.NOT_FOUND);
	}

	public NotFoundException(GlobalErrorCode code) {
		super(HttpStatus.NOT_FOUND, code);
	}

	public NotFoundException(GlobalErrorCode code, Object data) {
		super(HttpStatus.NOT_FOUND, code, data);
	}
}

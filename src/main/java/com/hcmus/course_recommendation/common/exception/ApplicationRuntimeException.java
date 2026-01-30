package com.hcmus.course_recommendation.common.exception;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@Getter
public abstract class ApplicationRuntimeException extends RuntimeException {
	private final HttpStatus status;
	private final GlobalErrorCode code;
	private final String data;

	protected ApplicationRuntimeException(HttpStatus status, GlobalErrorCode code) {
		super();
		this.status = status;
		this.code = code;
		this.data = null;
	}

	protected ApplicationRuntimeException(HttpStatus status, GlobalErrorCode code, Object data) {
		super();
		this.status = status;
		this.code = code;

		String dataString;

		try {
			dataString = new ObjectMapper().writeValueAsString(data);
		} catch (JsonProcessingException e) {
			throw new InternalServerErrorException(e);
		}

		this.data = dataString;
	}
}

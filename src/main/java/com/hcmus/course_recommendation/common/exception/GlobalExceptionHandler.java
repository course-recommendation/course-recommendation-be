package com.hcmus.course_recommendation.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.hcmus.course_recommendation.common.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(NoResourceFoundException.class)
	public void handleNoResourceFound(
		HttpServletRequest request,
		HttpServletResponse response,
		NoResourceFoundException ex) throws Exception {
		if ("GET".equalsIgnoreCase(request.getMethod())) {
			request.getRequestDispatcher("/index.html").forward(request, response);
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleException(Exception exception) {
		log.error(exception.getMessage(), exception);
		return ErrorResponse.create(GlobalErrorCode.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(ApplicationRuntimeException.class)
	public ResponseEntity<ErrorResponse> handleApplicationRuntimeException(
		ApplicationRuntimeException exception) {
		log.info(exception.getMessage(), exception);
		var response = ErrorResponse.create(
			exception.getCode(), exception.getData());
		return ResponseEntity.status(exception.getStatus().value()).body(response);
	}

	@ExceptionHandler(AuthorizationDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ErrorResponse handleAuthorizationDeniedException(
		AuthorizationDeniedException exception) {
		log.info(exception.getMessage(), exception);
		return ErrorResponse.create(GlobalErrorCode.FORBIDDEN);
	}

	@ExceptionHandler(BadJwtException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponse handleBadJwtException(BadJwtException exception) {
		log.info(exception.getMessage(), exception);
		return ErrorResponse.create(GlobalErrorCode.UNAUTHORIZED);
	}

	@ExceptionHandler(InternalServerErrorException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleInternalServerError(InternalServerErrorException exception) {
		log.info(exception.getMessage(), exception);
		return ErrorResponse.create(GlobalErrorCode.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleHttpRequestMethodNotSupportedException(
		HttpRequestMethodNotSupportedException exception) {
		log.info(exception.getMessage(), exception);
		return ErrorResponse.create(GlobalErrorCode.REQUEST_METHOD_NOT_SUPPORT);
	}
}


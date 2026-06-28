package com.hcmus.course_recommendation.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.auth.dto.PasswordResetConfirmRequest;
import com.hcmus.course_recommendation.auth.dto.PasswordResetRequestDto;
import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.tenant.TenantId;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PasswordResetController {
	private final PasswordResetService passwordResetService;

	@PostMapping("/auth/password-reset/request")
	public RestResponse<Void> requestReset(
		@RequestBody PasswordResetRequestDto request,
		@TenantId Long tenantId,
		HttpServletRequest servletRequest
	) {
		passwordResetService.requestReset(request.getEmail(), tenantId, servletRequest);
		return RestResponse.make();
	}

	@PostMapping("/auth/password-reset/confirm")
	public RestResponse<Void> confirmReset(@RequestBody PasswordResetConfirmRequest request) {
		passwordResetService.confirmReset(request.getToken(), request.getNewPassword());
		return RestResponse.make();
	}
}

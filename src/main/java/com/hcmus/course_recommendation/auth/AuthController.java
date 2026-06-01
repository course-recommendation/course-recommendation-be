package com.hcmus.course_recommendation.auth;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.auth.dto.LoginRequest;
import com.hcmus.course_recommendation.auth.dto.LoginResponse;
import com.hcmus.course_recommendation.auth.dto.RegisterAdminRequest;
import com.hcmus.course_recommendation.auth.dto.RegisterRequest;
import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.tenant.TenantId;
import com.hcmus.course_recommendation.user.User;
import com.hcmus.course_recommendation.user.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	private final UserService userService;

	@PostMapping("/auth/register")
	public RestResponse<Void> register(@RequestBody RegisterRequest registerRequest, @TenantId Long tenantId) {
		authService.register(registerRequest, tenantId);
		return RestResponse.make();
	}

	@PostMapping("/auth/register/admin")
	public RestResponse<Void> registerAdmin(@RequestBody RegisterAdminRequest registerRequest) {
		authService.registerAdmin(registerRequest);
		return RestResponse.make();
	}

	@PostMapping("/auth/login")
	public RestResponse<LoginResponse> login(@RequestBody LoginRequest loginRequest, @TenantId Long tenantId) {
		return RestResponse.make(authService.login(loginRequest, tenantId));
	}

	@GetMapping("/me")
	public RestResponse<User> getMe(Principal principal, @TenantId Long tenantId) {
		return RestResponse.make(userService.getUserById(principal.getName()));
	}
}

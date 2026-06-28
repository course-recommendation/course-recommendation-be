package com.hcmus.course_recommendation.user;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.common.exception.GlobalErrorCode;
import com.hcmus.course_recommendation.common.exception.NotFoundException;
import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.tenant.TenantId;
import com.hcmus.course_recommendation.user.dto.UpdateUserProfileRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	private final UserRepository userRepository;

	@GetMapping("/users")
	public RestResponse<List<User>> searchUsers(
		@RequestParam(required = false, defaultValue = "") String search,
		@TenantId Long tenantId) {
		return RestResponse.make(userService.searchUsers(search, tenantId));
	}

	@GetMapping("/users/{userId}")
	public RestResponse<User> getUserById(@PathVariable String userId) {
		var user = userService.getUserById(userId);
		if (user == null) {
			throw new NotFoundException(GlobalErrorCode.USER_NOT_FOUND);
		}
		return RestResponse.make(user);
	}

	@PutMapping("/me/done-first-login")
	public RestResponse<Void> doneFirstLogin(Principal principal, @RequestParam Algorithm algorithm,
		@TenantId Long tenantId) {
		userService.doneFirstLogin(principal.getName(), algorithm, tenantId);
		return RestResponse.make();
	}

	@GetMapping("/me/first-login")
	public RestResponse<Boolean> isFirstLogin(Principal principal, @RequestParam Algorithm algorithm,
		@TenantId Long tenantId) {
		return RestResponse.make(userService.isFirstLogin(principal.getName(), algorithm, tenantId));
	}

	@PutMapping("/me/profile")
	public RestResponse<User> updateProfile(Principal principal, @RequestBody UpdateUserProfileRequest request) {
		return RestResponse.make(userService.updateProfile(principal.getName(), request.getFullName(), request.getEmail()));
	}
}

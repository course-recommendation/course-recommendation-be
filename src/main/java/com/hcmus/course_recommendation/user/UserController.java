package com.hcmus.course_recommendation.user;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.course.model.Algorithm;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	private final UserRepository userRepository;

	@PutMapping("/me/done-first-login")
	public RestResponse<Void> doneFirstLogin(Principal principal, @RequestParam Algorithm algorithm) {
		userService.doneFirstLogin(principal.getName(), algorithm);
		return RestResponse.make();
	}

	@GetMapping("/me/first-login")
	public RestResponse<Boolean> isFirstLogin(Principal principal, @RequestParam Algorithm algorithm) {
		return RestResponse.make(userService.isFirstLogin(principal.getName(), algorithm));
	}
}

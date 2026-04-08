package com.hcmus.course_recommendation.user;

import java.security.Principal;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	private final UserRepository userRepository;

	@PutMapping("/me/complete-survey")
	public RestResponse<Void> doneDidSurvey(Principal principal) {
		var me = userService.getUserById(principal.getName());
		userRepository.save(me.toBuilder().didSurvey(true).build());
		return RestResponse.make();
	}
}

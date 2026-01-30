package com.hcmus.course_recommendation.auth.config;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.hcmus.course_recommendation.common.exception.GlobalErrorCode;
import com.hcmus.course_recommendation.common.exception.NotFoundException;
import com.hcmus.course_recommendation.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ApplicationUserDetailsService implements UserDetailsService {
	private final UserRepository userRepository;

	@Override
	@NullMarked
	public UserDetails loadUserByUsername(String username) {
		return userRepository.findById(username).map(ApplicationUserDetails::new)
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.USER_NOT_FOUND));
	}
}

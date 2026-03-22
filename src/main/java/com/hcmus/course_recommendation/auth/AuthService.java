package com.hcmus.course_recommendation.auth;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hcmus.course_recommendation.auth.config.JwtService;
import com.hcmus.course_recommendation.auth.dto.LoginRequest;
import com.hcmus.course_recommendation.auth.dto.LoginResponse;
import com.hcmus.course_recommendation.auth.dto.RegisterRequest;
import com.hcmus.course_recommendation.auth.mapper.AuthMapper;
import com.hcmus.course_recommendation.common.exception.BadRequestException;
import com.hcmus.course_recommendation.common.exception.GlobalErrorCode;
import com.hcmus.course_recommendation.user.Role;
import com.hcmus.course_recommendation.user.User;
import com.hcmus.course_recommendation.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final AuthMapper authMapper;

	public void register(RegisterRequest request) {

		var user = authMapper.toUser(request).toBuilder()
			.roles(List.of(Role.USER))
			.password(passwordEncoder.encode(request.getPassword()))
			.build();

		try {
			userRepository.save(user);
		} catch (DataIntegrityViolationException exception) {
			throw new BadRequestException(GlobalErrorCode.EMAIL_DUPLICATED);
		}
	}

	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new BadRequestException(GlobalErrorCode.EMAIL_NOT_FOUND));

		try {
			authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(user.getId(), request.getPassword()));
		} catch (BadCredentialsException e) {
			throw new BadRequestException(GlobalErrorCode.WRONG_PASSWORD);
		}

		String accessToken = jwtService.generateToken(user.getId());

		return LoginResponse.builder().accessToken(accessToken).build();
	}
}

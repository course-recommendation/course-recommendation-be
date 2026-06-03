package com.hcmus.course_recommendation.auth;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.auth.config.JwtService;
import com.hcmus.course_recommendation.auth.dto.LoginRequest;
import com.hcmus.course_recommendation.auth.dto.LoginResponse;
import com.hcmus.course_recommendation.auth.dto.RegisterAdminRequest;
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

	@Transactional
	public void register(RegisterRequest request, Long tenantId) {
		var user = authMapper.toUser(request).toBuilder()
			.tenantId(tenantId)
			.roles(List.of(Role.USER))
			.password(passwordEncoder.encode(request.getPassword()))
			.build();

		if (userRepository.findByEmailAndTenantId(request.getEmail(), tenantId).isPresent()) {
			throw new BadRequestException(GlobalErrorCode.EMAIL_DUPLICATED);
		}

		var savedUser = userRepository.save(user);

		// Save dummy avatar
		userRepository.save(savedUser.toBuilder()
			.avatarUrl(String.format("https://picsum.photos/seed/%s/1600/900", savedUser.getId()))
			.build());
	}

	@Transactional
	public void registerAdmin(RegisterAdminRequest request) {

		var user = User.builder()
			.email(request.getEmail())
			.password(passwordEncoder.encode(request.getPassword()))
			.roles(List.of(Role.ADMIN))
			.tenantId(request.getTenantId())
			.build();

		if (userRepository.findByEmailAndTenantId(request.getEmail(), request.getTenantId()).isPresent()) {
			throw new BadRequestException(GlobalErrorCode.EMAIL_DUPLICATED);
		}

		var savedUser = userRepository.save(user);

		// Save dummy avatar
		userRepository.save(savedUser.toBuilder()
			.avatarUrl(String.format("https://picsum.photos/seed/%s/1600/900", savedUser.getId()))
			.build());
	}

	public LoginResponse login(LoginRequest request, Long tenantId) {
		User user = userRepository.findByEmailAndTenantId(request.getEmail(), tenantId)
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

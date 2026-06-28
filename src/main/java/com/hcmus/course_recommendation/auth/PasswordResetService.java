package com.hcmus.course_recommendation.auth;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.common.exception.BadRequestException;
import com.hcmus.course_recommendation.common.exception.GlobalErrorCode;
import com.hcmus.course_recommendation.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
	private final UserRepository userRepository;
	private final PasswordResetRepository passwordResetRepository;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void requestReset(String email, Long tenantId, HttpServletRequest servletRequest) {
		var user = userRepository.findByEmailAndTenantId(email, tenantId)
			.orElseThrow(() -> new BadRequestException(GlobalErrorCode.EMAIL_NOT_FOUND));

		String token = UUID.randomUUID().toString();
		passwordResetRepository.save(PasswordReset.builder()
			.userId(user.getId())
			.token(token)
			.expires(Instant.now().plusSeconds(3600))
			.build());

		String frontendUrl = servletRequest.getHeader("Origin");
		String resetLink = frontendUrl + "/reset-password?token=" + token;
		emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
	}

	@Transactional
	public void confirmReset(String token, String newPassword) {
		var reset = passwordResetRepository.findByToken(token)
			.orElseThrow(() -> new BadRequestException(GlobalErrorCode.BAD_REQUEST));

		if (reset.isUsed() || reset.getExpires().isBefore(Instant.now())) {
			throw new BadRequestException(GlobalErrorCode.BAD_REQUEST);
		}

		var user = userRepository.findById(reset.getUserId())
			.orElseThrow(() -> new BadRequestException(GlobalErrorCode.USER_NOT_FOUND));

		userRepository.save(user.toBuilder()
			.password(passwordEncoder.encode(newPassword))
			.build());

		reset.setUsed(true);
		passwordResetRepository.save(reset);
	}
}

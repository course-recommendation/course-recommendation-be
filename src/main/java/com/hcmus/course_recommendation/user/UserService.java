package com.hcmus.course_recommendation.user;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.model.Algorithm;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final UserFirstLoginRepository userFirstLoginRepository;

	@Transactional(readOnly = true)
	public User getUserById(String userId) {
		return userRepository.findById(userId).orElse(null);
	}

	@Transactional(readOnly = true)
	public List<User> findUsersByIds(List<String> userIds) {
		return userRepository.findByIdIn(userIds);
	}

	@Transactional(readOnly = true)
	public Map<String, User> getUserIdToUserMapByUserIds(List<String> userIds) {
		return findUsersByIds(userIds).stream().collect(
			Collectors.toMap(User::getId, java.util.function.Function.identity())
		);
	}

	@Transactional(readOnly = true)
	public boolean isFirstLogin(String userId, Algorithm algorithm, Long tenantId) {
		return userFirstLoginRepository.findByUserIdAndAlgorithmAndTenantId(userId, algorithm, tenantId).isEmpty();
	}

	@Transactional
	public void doneFirstLogin(String userId, Algorithm algorithm, Long tenantId) {
		userFirstLoginRepository.save(UserFirstLogin.builder()
			.userId(userId)
			.algorithm(algorithm)
			.tenantId(tenantId)
			.build());
	}

	@Transactional(readOnly = true)
	public List<User> searchUsers(String search, Long tenantId) {
		return userRepository.findByTenantIdAndFullNameContainingIgnoreCase(tenantId, search);
	}

	@Transactional
	public User updateProfile(String userId, String fullName, String email) {
		var user = userRepository.findById(userId)
			.orElseThrow(() -> new com.hcmus.course_recommendation.common.exception.NotFoundException(
				com.hcmus.course_recommendation.common.exception.GlobalErrorCode.USER_NOT_FOUND));
		if (email != null && !email.equals(user.getEmail())) {
			userRepository.findByEmailAndTenantId(email, user.getTenantId()).ifPresent(existing -> {
				throw new com.hcmus.course_recommendation.common.exception.BadRequestException(
					com.hcmus.course_recommendation.common.exception.GlobalErrorCode.EMAIL_DUPLICATED);
			});
		}
		var builder = user.toBuilder().fullName(fullName);
		if (email != null) builder.email(email);
		return userRepository.save(builder.build());
	}
}

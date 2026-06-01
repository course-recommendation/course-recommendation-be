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
}

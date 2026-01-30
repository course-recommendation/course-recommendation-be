package com.hcmus.course_recommendation.user;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

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
}

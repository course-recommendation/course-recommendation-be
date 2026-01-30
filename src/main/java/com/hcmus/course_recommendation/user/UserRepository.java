package com.hcmus.course_recommendation.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
	Optional<User> findByEmail(String email);

	List<User> findByIdIn(List<String> ids);
}

package com.hcmus.course_recommendation.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.course.model.Algorithm;

public interface UserFirstLoginRepository extends JpaRepository<UserFirstLogin, Long> {
	Optional<UserFirstLogin> findByUserIdAndAlgorithm(String userId, Algorithm algorithm);
}

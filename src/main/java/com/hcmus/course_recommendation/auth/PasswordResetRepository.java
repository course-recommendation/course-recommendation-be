package com.hcmus.course_recommendation.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, String> {
	Optional<PasswordReset> findByToken(String token);
}

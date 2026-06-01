package com.hcmus.course_recommendation.recommendation.fs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.recommendation.fs.model.UserPreference;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
	Optional<UserPreference> findByAlgorithmAndTenantIdAndUserId(Algorithm algorithm, Long tenantId, String userId);
}

package com.hcmus.course_recommendation.recommendation.fs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.recommendation.fs.model.UserPreference;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
	Optional<UserPreference> findByAlgorithmAndTenantIdAndUserId(Algorithm algorithm, Long tenantId, String userId);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserPreference p WHERE p.userId = :userId")
	void deleteByUserId(@Param("userId") String userId);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserPreference p WHERE p.userId IN :userIds")
	void deleteByUserIdIn(@Param("userIds") List<String> userIds);
}

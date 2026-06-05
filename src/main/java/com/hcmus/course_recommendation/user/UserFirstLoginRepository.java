package com.hcmus.course_recommendation.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.model.Algorithm;

public interface UserFirstLoginRepository extends JpaRepository<UserFirstLogin, Long> {
	Optional<UserFirstLogin> findByUserIdAndAlgorithmAndTenantId(String userId, Algorithm algorithm, Long tenantId);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserFirstLogin f WHERE f.userId = :userId")
	void deleteByUserId(@Param("userId") String userId);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserFirstLogin f WHERE f.userId IN :userIds")
	void deleteByUserIdIn(@Param("userIds") List<String> userIds);
}

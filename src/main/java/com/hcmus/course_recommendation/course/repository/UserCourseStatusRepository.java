package com.hcmus.course_recommendation.course.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.UserCourseStatus;
import com.hcmus.course_recommendation.course.model.UserCourseStatusEnum;

public interface UserCourseStatusRepository extends JpaRepository<UserCourseStatus, Long> {

	@Query("""
		SELECT ucs
		FROM UserCourseStatus ucs
		JOIN Course c
		ON ucs.courseId = c.id
		WHERE TRUE
		AND ucs.userId = :userId
		AND ucs.status = :status
		AND c.algorithm = :algorithm
		AND c.tenantId = :tenantId
		""")
	List<UserCourseStatus> findByUserIdAndStatusAndAlgorithmAndTenantId(String userId, UserCourseStatusEnum status,
		Algorithm algorithm, Long tenantId);

	Optional<UserCourseStatus> findByUserIdAndCourseIdAndStatus(String userId, Long courseId,
		UserCourseStatusEnum status);

	@Query("""
		SELECT ucs
		FROM UserCourseStatus ucs
		JOIN Course c
		ON ucs.courseId = c.id
		WHERE TRUE
		AND ucs.userId = :userId
		AND c.algorithm = :algorithm
		AND c.tenantId = :tenantId
		AND ucs.status <> :status
		AND ucs.courseId IN (:courseIds)
		""")
	List<UserCourseStatus> findByUserIdAndAlgorithmAndTenantIdAndNotStatusAndCourseIdIn(String userId,
		Algorithm algorithm, Long tenantId, UserCourseStatusEnum status, List<Long> courseIds);

	List<UserCourseStatus> findByUserId(String userId);

	@Modifying
	@Query("""
		DELETE FROM UserCourseStatus
		WHERE userId = :userId
		AND courseId = :courseId
		AND status <> :status
		""")
	void deleteByUserIdAndCourseIdAndStatusNot(String userId, Long courseId, UserCourseStatusEnum status);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserCourseStatus s WHERE s.userId = :userId")
	void deleteByUserId(@Param("userId") String userId);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserCourseStatus s WHERE s.userId IN :userIds")
	void deleteByUserIdIn(@Param("userIds") List<String> userIds);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserCourseStatus s WHERE s.courseId = :courseId")
	void deleteByCourseId(@Param("courseId") Long courseId);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserCourseStatus s WHERE s.courseId IN :courseIds")
	void deleteByCourseIdIn(@Param("courseIds") List<Long> courseIds);
}

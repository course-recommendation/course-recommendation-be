package com.hcmus.course_recommendation.course.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.UserCourseRating;

public interface UserCourseRatingRepository extends JpaRepository<UserCourseRating, Long> {

	List<UserCourseRating> findByUserId(String userId);

	Optional<UserCourseRating> findByUserIdAndCourseIdAndAttributeId(String userId, Long courseId, Long attributeId);

	@Query("""
		SELECT ucr
		FROM UserCourseRating ucr
		JOIN Course c ON ucr.courseId = c.id
		WHERE c.algorithm = :algorithm
		AND c.tenantId = :tenantId
		""")
	List<UserCourseRating> findByAlgorithmAndTenantId(Algorithm algorithm, Long tenantId);

	@Query(value = """
		SELECT ucr FROM UserCourseRating ucr
		JOIN Course c ON c.id = ucr.courseId
		WHERE c.tenantId = :tenantId
		AND (:userId IS NULL OR ucr.userId LIKE CONCAT('%', :userId, '%'))
		AND (:courseId IS NULL OR ucr.courseId = :courseId)
		AND (:attributeId IS NULL OR ucr.attributeId = :attributeId)
		""",
		countQuery = """
		SELECT COUNT(ucr) FROM UserCourseRating ucr
		JOIN Course c ON c.id = ucr.courseId
		WHERE c.tenantId = :tenantId
		AND (:userId IS NULL OR ucr.userId LIKE CONCAT('%', :userId, '%'))
		AND (:courseId IS NULL OR ucr.courseId = :courseId)
		AND (:attributeId IS NULL OR ucr.attributeId = :attributeId)
		""")
	Page<UserCourseRating> findByTenantIdAndFilters(
		@Param("tenantId") Long tenantId,
		@Param("userId") String userId,
		@Param("courseId") Long courseId,
		@Param("attributeId") Long attributeId,
		Pageable pageable);

	List<UserCourseRating> findByAttributeId(Long attributeId);

	List<UserCourseRating> findByCourseId(Long courseId);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserCourseRating r WHERE r.attributeId = :attributeId")
	void deleteByAttributeId(@Param("attributeId") Long attributeId);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserCourseRating r WHERE r.attributeId IN :attributeIds")
	void deleteByAttributeIdIn(@Param("attributeIds") List<Long> attributeIds);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserCourseRating r WHERE r.courseId = :courseId")
	void deleteByCourseId(@Param("courseId") Long courseId);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserCourseRating r WHERE r.courseId IN :courseIds")
	void deleteByCourseIdIn(@Param("courseIds") List<Long> courseIds);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserCourseRating r WHERE r.userId = :userId")
	void deleteByUserId(@Param("userId") String userId);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserCourseRating r WHERE r.userId IN :userIds")
	void deleteByUserIdIn(@Param("userIds") List<String> userIds);
}

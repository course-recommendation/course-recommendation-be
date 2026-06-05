package com.hcmus.course_recommendation.course.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.UserCourseRating;

public interface UserCourseRatingRepository extends JpaRepository<UserCourseRating, Long> {

	List<UserCourseRating> findByUserId(String userId);

	Optional<UserCourseRating> findByUserIdAndCourseIdAndAttributeId(String userId, Long courseId,
		Long attributeId);

	@Query("""
		SELECT ucr
		FROM UserCourseRating ucr
		JOIN Course c ON ucr.courseId = c.id
		WHERE c.algorithm = :algorithm
		AND c.tenantId = :tenantId
		""")
	List<UserCourseRating> findByAlgorithmAndTenantId(Algorithm algorithm, Long tenantId);
}

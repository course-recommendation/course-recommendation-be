package com.hcmus.course_recommendation.course.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.UserCourseSatisfaction;

public interface UserCourseSatisfactionRepository extends JpaRepository<UserCourseSatisfaction, Long> {

	@Query("""
		SELECT ucs
		FROM UserCourseSatisfaction ucs
		JOIN Course c ON ucs.courseId = c.id
		WHERE c.algorithm = :algorithm
		AND c.tenantId = :tenantId
		""")
	List<UserCourseSatisfaction> findByAlgorithmAndTenantId(Algorithm algorithm, Long tenantId);

	List<UserCourseSatisfaction> findByUserId(String userId);

	Optional<UserCourseSatisfaction> findByUserIdAndCourseId(String userId, Long courseId);

	void deleteByCourseId(Long courseId);

	void deleteByUserId(String userId);
}

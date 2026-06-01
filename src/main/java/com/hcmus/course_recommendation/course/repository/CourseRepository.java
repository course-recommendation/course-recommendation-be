package com.hcmus.course_recommendation.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
	List<Course> findByAlgorithmAndTenantId(Algorithm algorithm, Long tenantId);

	List<Course> findByAlgorithmAndTenantIdAndNameLike(Algorithm algorithm, Long tenantId, String name);

	List<Course> findByAlgorithmAndTenantIdAndCodeIn(Algorithm algorithm, Long tenantId,
		List<String> courseCodes);

	List<Course> findByIdIn(List<Long> ids);

	long countByAlgorithmAndTenantId(Algorithm algorithm, Long tenantId);

	boolean existsByIdAndTenantId(Long id, Long tenantId);
}

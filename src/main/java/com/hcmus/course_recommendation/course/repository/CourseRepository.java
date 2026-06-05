package com.hcmus.course_recommendation.course.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Course;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
	List<Course> findByAlgorithmAndTenantId(Algorithm algorithm, Long tenantId);

	List<Course> findByAlgorithmAndTenantIdAndNameLike(Algorithm algorithm, Long tenantId, String name);

	List<Course> findByAlgorithmAndTenantIdAndCodeIn(Algorithm algorithm, Long tenantId, List<String> courseCodes);

	List<Course> findByIdIn(List<Long> ids);

	long countByAlgorithmAndTenantId(Algorithm algorithm, Long tenantId);

	long countByTenantId(Long tenantId);

	boolean existsByIdAndTenantId(Long id, Long tenantId);

	Optional<Course> findByCodeAndTenantId(String code, Long tenantId);

	List<Course> findByTenantId(Long tenantId);
}

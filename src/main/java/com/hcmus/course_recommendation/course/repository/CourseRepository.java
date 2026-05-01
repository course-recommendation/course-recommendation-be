package com.hcmus.course_recommendation.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
	List<Course> findByAlgorithm(Algorithm algorithm);

	List<Course> findByAlgorithmAndNameLike(Algorithm algorithm, String name);

	List<Course> findByAlgorithmAndCodeIn(Algorithm algorithm,
		List<String> courseCodes);

	List<Course> findByIdIn(List<Long> ids);

	long countByAlgorithm(Algorithm algorithm);
}

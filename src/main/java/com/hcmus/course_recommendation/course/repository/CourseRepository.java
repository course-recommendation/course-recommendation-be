package com.hcmus.course_recommendation.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.model.Dataset;

public interface CourseRepository extends JpaRepository<Course, Long> {
	List<Course> findByAlgorithmAndDataset(Algorithm algorithm, Dataset dataset);

	List<Course> findByAlgorithmAndDatasetAndNameLike(Algorithm algorithm, Dataset dataset, String name);

	List<Course> findByAlgorithmAndDatasetAndCodeIn(Algorithm algorithm, Dataset dataset,
		List<String> courseCodes);

	List<Course> findByDataset(Dataset dataset);

	List<Course> findByIdIn(List<Long> ids);
}

package com.hcmus.course_recommendation.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.model.CourseAlgorithm;
import com.hcmus.course_recommendation.course.model.CourseDataset;

public interface CourseRepository extends JpaRepository<Course, Long> {
	List<Course> findByIdIn(List<String> ids);

	List<Course> findByAlgorithmAndDataset(CourseAlgorithm algorithm, CourseDataset dataset);

	List<Course> findByAlgorithmAndDatasetAndIdIn(CourseAlgorithm algorithm, CourseDataset dataset, List<String> ids);
}

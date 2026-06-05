package com.hcmus.course_recommendation.course.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.course.model.FsCourseSentiment;

public interface FsCourseSentimentRepository extends JpaRepository<FsCourseSentiment, Long> {
	Optional<FsCourseSentiment> findByCourseId(Long courseId);

	List<FsCourseSentiment> findByCourseIdIn(List<Long> courseIds);
}

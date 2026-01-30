package com.hcmus.course_recommendation.recommendation.fs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.course.model.CourseDataset;
import com.hcmus.course_recommendation.recommendation.fs.model.FSUserPreference;

public interface FSUserPreferenceRepository extends JpaRepository<FSUserPreference, Long> {
	Optional<FSUserPreference> findByDatasetAndUserId(CourseDataset dataset, String userId);
}

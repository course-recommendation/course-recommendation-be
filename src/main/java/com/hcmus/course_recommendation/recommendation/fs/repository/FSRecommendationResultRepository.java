package com.hcmus.course_recommendation.recommendation.fs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hcmus.course_recommendation.course.model.CourseDataset;
import com.hcmus.course_recommendation.recommendation.fs.model.FSRecommendationResult;

public interface FSRecommendationResultRepository extends JpaRepository<FSRecommendationResult, Long> {
	@Query("""
		SELECT r
		FROM fs_recommendation_result r
		WHERE r.dataset = :dataset
		AND r.userId = :userId
		ORDER BY r.id DESC
		LIMIT 1
		""")
	Optional<FSRecommendationResult> getLatestFSRecommendationResult(CourseDataset dataset, String userId);
}

package com.hcmus.course_recommendation.recommendation.fs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Dataset;
import com.hcmus.course_recommendation.recommendation.fs.model.RecommendationResult;

public interface RecommendationResultRepository extends JpaRepository<RecommendationResult, Long> {
	@Query("""
		SELECT r
		FROM RecommendationResult r
		WHERE r.dataset = :dataset
		AND r.algorithm = :algorithm
		AND r.userId = :userId
		ORDER BY r.id DESC
		LIMIT 1
		""")
	Optional<RecommendationResult> getLatestFSRecommendationResult(Dataset dataset, Algorithm algorithm,
		String userId);
}

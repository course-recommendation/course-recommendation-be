package com.hcmus.course_recommendation.course.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Dataset;
import com.hcmus.course_recommendation.course.model.UserCourseRating;

public interface UserCourseRatingRepository extends JpaRepository<UserCourseRating, Long> {

	List<UserCourseRating> findByUserId(String userId);

	Optional<UserCourseRating> findByUserIdAndCourseIdAndAttributeValue(String userId, Long courseId,
		String attributeValue);

	@Query("""
		SELECT ucr
		FROM UserCourseRating ucr
		JOIN Course c ON ucr.courseId = c.id
		WHERE c.dataset = :dataset
		AND c.algorithm = :algorithm
		""")
	List<UserCourseRating> findByAlgorithmAndDataset(Algorithm algorithm, Dataset dataset);
}

package com.hcmus.course_recommendation.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.hcmus.course_recommendation.course.model.CourseAlgorithm;
import com.hcmus.course_recommendation.course.model.CourseDataset;
import com.hcmus.course_recommendation.course.model.UserCourse;
import com.hcmus.course_recommendation.course.model.UserCourseStatus;

public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {

	@Query("""
		SELECT uc
		FROM UserCourse uc
		JOIN Course c
		ON uc.courseId = c.id
		WHERE TRUE
		AND uc.userId = :userId
		AND uc.status = :status
		AND c.algorithm = :algorithm
		AND c.dataset = :dataset
		""")
	List<UserCourse> findByUserIdAndStatusAndCourseDomain(String userId, UserCourseStatus status,
		CourseAlgorithm algorithm, CourseDataset dataset);

	List<UserCourse> findByUserId(String userId);

	@Modifying
	@Query("""
		DELETE FROM UserCourse
		WHERE userId = :userId
		AND courseId = :courseId
		""")
	void deleteByUserIdAndCourseId(String userId, String courseId);
}

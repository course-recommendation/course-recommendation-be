package com.hcmus.course_recommendation.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Dataset;
import com.hcmus.course_recommendation.course.model.UserCourseStatus;
import com.hcmus.course_recommendation.course.model.UserCourseStatusEnum;

public interface UserCourseStatusRepository extends JpaRepository<UserCourseStatus, Long> {

	@Query("""
		SELECT ucs
		FROM UserCourseStatus ucs
		JOIN Course c
		ON ucs.courseId = c.id
		WHERE TRUE
		AND ucs.userId = :userId
		AND ucs.status = :status
		AND c.algorithm = :algorithm
		AND c.dataset = :dataset
		""")
	List<UserCourseStatus> findByUserIdAndStatusAndDomain(String userId, UserCourseStatusEnum status,
		Algorithm algorithm,
		Dataset dataset);

	List<UserCourseStatus> findByUserId(String userId);

	@Modifying
	@Query("""
		DELETE FROM UserCourseStatus
		WHERE userId = :userId
		AND courseId = :courseId
		""")
	void deleteByUserIdAndCourseId(String userId, String courseId);
}

package com.hcmus.course_recommendation.course.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.model.FsCourseSentiment;

public interface FsCourseSentimentRepository extends JpaRepository<FsCourseSentiment, Long> {
	Optional<FsCourseSentiment> findByCourseId(Long courseId);

	List<FsCourseSentiment> findByCourseIdIn(List<Long> courseIds);

	@Modifying
	@Transactional
	@Query("""
		DELETE FROM FsCourseSentiment s
		WHERE s.courseId IN (SELECT c.id FROM Course c WHERE c.tenantId = :tenantId)
		""")
	void deleteByTenantId(@Param("tenantId") Long tenantId);
}

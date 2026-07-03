package com.hcmus.course_recommendation.discuss.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.discuss.model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
	Page<Post> findByAlgorithmAndTenantIdAndCourseCodeIn(Algorithm algorithm, Long tenantId,
		List<String> courseIds, Pageable pageable);

	Page<Post> findByAlgorithmAndTenantIdAndCourseCodeInAndUserIdIn(Algorithm algorithm, Long tenantId,
		List<String> courseIds, List<String> userIds, Pageable pageable);
}

package com.hcmus.course_recommendation.discuss.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.discuss.model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
	List<Post> findByAlgorithmAndTenantIdAndCourseCodeIn(Algorithm algorithm, Long tenantId,
		List<String> courseIds, Sort sort);

	List<Post> findByAlgorithmAndTenantIdAndCourseCodeInAndUserIdIn(Algorithm algorithm, Long tenantId,
		List<String> courseIds, List<String> userIds, Sort sort);
}

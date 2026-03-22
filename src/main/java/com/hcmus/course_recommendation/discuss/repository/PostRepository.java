package com.hcmus.course_recommendation.discuss.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Dataset;
import com.hcmus.course_recommendation.discuss.model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
	List<Post> findByAlgorithmAndDatasetAndCourseCodeIn(Algorithm algorithm, Dataset dataset, List<String> courseIds,
		Sort sort);
}

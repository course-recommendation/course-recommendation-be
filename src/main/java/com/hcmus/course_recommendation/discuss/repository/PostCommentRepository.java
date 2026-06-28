package com.hcmus.course_recommendation.discuss.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.discuss.model.PostComment;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

	List<PostComment> findByPostIdOrderByIdDesc(Long postId);
}

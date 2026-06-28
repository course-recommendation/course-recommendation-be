package com.hcmus.course_recommendation.discuss.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.discuss.model.PostCommentVote;

public interface PostCommentVoteRepository extends JpaRepository<PostCommentVote, Long> {
	Optional<PostCommentVote> findByCommentIdAndUserId(Long commentId, String userId);

	List<PostCommentVote> findByCommentIdIn(List<Long> commentIds);

	void deleteByCommentIdAndUserId(Long commentId, String userId);
}

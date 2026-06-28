package com.hcmus.course_recommendation.discuss.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.discuss.model.PostVote;

public interface PostVoteRepository extends JpaRepository<PostVote, Long> {
	Optional<PostVote> findByPostIdAndUserId(Long postId, String userId);

	List<PostVote> findByPostIdIn(List<Long> postIds);

	void deleteByPostIdAndUserId(Long postId, String userId);
}

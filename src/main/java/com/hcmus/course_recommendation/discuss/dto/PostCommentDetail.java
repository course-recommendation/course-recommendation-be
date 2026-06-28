package com.hcmus.course_recommendation.discuss.dto;

import java.util.List;

import com.hcmus.course_recommendation.discuss.model.PostComment;
import com.hcmus.course_recommendation.discuss.model.VoteType;
import com.hcmus.course_recommendation.user.User;

import lombok.Builder;

@Builder
public record PostCommentDetail(
	PostComment postComment,
	User user,
	int voteCount,
	VoteType userVote,
	List<PostCommentDetail> replies
) {
}

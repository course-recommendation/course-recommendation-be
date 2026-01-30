package com.hcmus.course_recommendation.discuss.dto;

import com.hcmus.course_recommendation.discuss.model.PostComment;
import com.hcmus.course_recommendation.user.User;

import lombok.Builder;

@Builder
public record PostCommentDetail(
	PostComment postComment,
	User user
) {
}

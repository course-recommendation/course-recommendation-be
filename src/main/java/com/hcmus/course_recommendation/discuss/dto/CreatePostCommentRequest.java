package com.hcmus.course_recommendation.discuss.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CreatePostCommentRequest {
	@JsonIgnore
	private String userId;
	@JsonIgnore
	private Long postId;
	private String content;
	private Long parentCommentId;
}

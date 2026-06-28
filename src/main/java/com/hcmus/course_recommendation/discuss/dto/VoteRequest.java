package com.hcmus.course_recommendation.discuss.dto;

import com.hcmus.course_recommendation.discuss.model.VoteType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoteRequest {
	private VoteType voteType;
}

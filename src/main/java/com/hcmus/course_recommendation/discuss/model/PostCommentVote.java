package com.hcmus.course_recommendation.discuss.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post_comment_vote")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostCommentVote {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long commentId;
	private String userId;
	@Enumerated(EnumType.STRING)
	private VoteType voteType;
}

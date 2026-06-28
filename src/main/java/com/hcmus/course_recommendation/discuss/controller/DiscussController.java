package com.hcmus.course_recommendation.discuss.controller;

import java.security.Principal;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.discuss.dto.CreatePostCommentRequest;
import com.hcmus.course_recommendation.discuss.dto.CreatePostRequest;
import com.hcmus.course_recommendation.discuss.dto.FindPostDetailsRequest;
import com.hcmus.course_recommendation.discuss.dto.PostCommentDetail;
import com.hcmus.course_recommendation.discuss.dto.PostDetail;
import com.hcmus.course_recommendation.discuss.dto.VoteRequest;
import com.hcmus.course_recommendation.discuss.service.DiscussService;
import com.hcmus.course_recommendation.tenant.TenantId;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DiscussController {
	private final DiscussService discussService;

	@PostMapping("/posts")
	public RestResponse<Void> createPost(@RequestBody CreatePostRequest request, Principal principal,
		@TenantId Long tenantId) {
		request.setUserId(principal.getName());
		request.setTenantId(tenantId);
		discussService.createPost(request);
		return RestResponse.make();
	}

	@GetMapping("/posts")
	public RestResponse<List<PostDetail>> findPostDetails(@ParameterObject FindPostDetailsRequest request,
		@ParameterObject Sort sort, @TenantId Long tenantId, Principal principal) {
		request.setTenantId(tenantId);
		return RestResponse.make(discussService.findPostDetails(request, sort, principal.getName()));
	}

	@PostMapping("/posts/{postId}/vote")
	public RestResponse<Void> votePost(@PathVariable Long postId, @RequestBody VoteRequest request,
		Principal principal) {
		discussService.votePost(postId, principal.getName(), request);
		return RestResponse.make();
	}

	@DeleteMapping("/posts/{postId}/vote")
	public RestResponse<Void> removePostVote(@PathVariable Long postId, Principal principal) {
		discussService.removePostVote(postId, principal.getName());
		return RestResponse.make();
	}

	@PostMapping("/posts/{postId}/comments")
	public RestResponse<Void> createPostComment(@RequestBody CreatePostCommentRequest request, Principal principal,
		@PathVariable Long postId, @TenantId Long tenantId) {
		request.setUserId(principal.getName());
		request.setPostId(postId);
		discussService.createPostComment(request);
		return RestResponse.make();
	}

	@GetMapping("/posts/{postId}/comments")
	public RestResponse<List<PostCommentDetail>> findPostCommentsByPostId(@PathVariable Long postId,
		@TenantId Long tenantId, Principal principal) {
		return RestResponse.make(discussService.findPostCommentsByPostId(postId, principal.getName()));
	}

	@PostMapping("/posts/{postId}/comments/{commentId}/vote")
	public RestResponse<Void> votePostComment(@PathVariable Long postId, @PathVariable Long commentId,
		@RequestBody VoteRequest request, Principal principal) {
		discussService.votePostComment(commentId, principal.getName(), request);
		return RestResponse.make();
	}

	@DeleteMapping("/posts/{postId}/comments/{commentId}/vote")
	public RestResponse<Void> removePostCommentVote(@PathVariable Long postId, @PathVariable Long commentId,
		Principal principal) {
		discussService.removePostCommentVote(commentId, principal.getName());
		return RestResponse.make();
	}
}

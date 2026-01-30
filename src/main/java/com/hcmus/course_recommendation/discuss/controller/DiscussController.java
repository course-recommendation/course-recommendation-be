package com.hcmus.course_recommendation.discuss.controller;

import java.security.Principal;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Sort;
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
import com.hcmus.course_recommendation.discuss.service.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DiscussController {
	private final PostService postService;

	@PostMapping("/posts")
	public RestResponse<Void> createPost(@RequestBody CreatePostRequest request, Principal principal) {
		request.setUserId(principal.getName());
		postService.createPost(request);
		return RestResponse.make();
	}

	@GetMapping("/posts")
	public RestResponse<List<PostDetail>> findPostDetails(@ParameterObject FindPostDetailsRequest request,
		@ParameterObject Sort sort) {
		return RestResponse.make(postService.findPostDetails(request, sort));
	}

	@PostMapping("/posts/{postId}/comments")
	public RestResponse<Void> createPostComment(@RequestBody CreatePostCommentRequest request, Principal principal,
		@PathVariable Long postId) {
		request.setUserId(principal.getName());
		request.setPostId(postId);
		postService.createPostComment(request);

		return RestResponse.make();
	}

	@GetMapping("/posts/{postId}/comments")
	public RestResponse<List<PostCommentDetail>> findPostCommentsByPostId(@PathVariable Long postId) {
		return RestResponse.make(postService.findPostCommentsByPostId(postId));
	}
}

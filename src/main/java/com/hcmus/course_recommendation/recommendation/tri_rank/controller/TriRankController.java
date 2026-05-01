package com.hcmus.course_recommendation.recommendation.tri_rank.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.recommendation.tri_rank.dto.ServerTriRankRecommendationResult;
import com.hcmus.course_recommendation.recommendation.tri_rank.dto.TriRankRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.tri_rank.service.TriRankRecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TriRankController {
	private final TriRankRecommendationService triRankRecommendationService;

	@PostMapping("/tri-rank/recommendation")
	public RestResponse<ServerTriRankRecommendationResult> getRecommendation(
		@RequestBody TriRankRecommendationRequest request, Principal principal) {
		request.setUserId(principal.getName());
		return RestResponse.make(triRankRecommendationService.getTriRankRecommendation(request));
	}

	@GetMapping("/tri-rank/latest-recommendation")
	public RestResponse<ServerTriRankRecommendationResult> getLatestRecommendationResult(Principal principal) {
		return RestResponse.make(
			triRankRecommendationService.getLatestTriRankRecommendationResult(principal.getName()));
	}
}


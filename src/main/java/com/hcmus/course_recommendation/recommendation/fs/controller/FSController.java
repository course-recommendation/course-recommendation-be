package com.hcmus.course_recommendation.recommendation.fs.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.recommendation.RecommendationService;
import com.hcmus.course_recommendation.recommendation.fs.dto.FSRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.fs.dto.FSRefinedRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.fs.dto.ServerFSRecommendationResult;
import com.hcmus.course_recommendation.recommendation.fs.service.FSService;
import com.hcmus.course_recommendation.tenant.TenantId;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FSController {
	private final FSService fsService;
	private final RecommendationService recommendationService;

	@PostMapping("/fs/recommendation")
	public RestResponse<ServerFSRecommendationResult> getFeatureSentimentRecommendation(
		@RequestBody FSRecommendationRequest request, Principal principal, @TenantId Long tenantId) {
		request.setUserId(principal.getName());
		request.setTenantId(tenantId);
		return RestResponse.make(fsService.getFSRecommendation(request));
	}

	@PostMapping("/fs/recommendation/refined")
	public RestResponse<ServerFSRecommendationResult> getFeatureSentimentRefinedRecommendation(
		@RequestBody FSRefinedRecommendationRequest request, Principal principal, @TenantId Long tenantId) {
		request.setUserId(principal.getName());
		request.setTenantId(tenantId);
		return RestResponse.make(fsService.getFsRefinedRecommendation(request));
	}

	@GetMapping("/fs/latest-recommendation")
	public RestResponse<ServerFSRecommendationResult> getLatestRecommendationResult(
		Principal principal, @TenantId Long tenantId) {
		return RestResponse.make(fsService.getLatestFsRecommendationResult(principal.getName(), tenantId));
	}

	@PostMapping("/fs/update-item-sentiments")
	public RestResponse<Void> updateItemSentiments(@TenantId Long tenantId) {
		fsService.updateItemSentiments(tenantId);

		return RestResponse.make();
	}

	@PutMapping("/fs/update-sentiments")
	public RestResponse<Void> updateCoursesSentiments(@TenantId Long tenantId) {
		fsService.updateCoursesSentiments(tenantId);
		return RestResponse.make();
	}
}

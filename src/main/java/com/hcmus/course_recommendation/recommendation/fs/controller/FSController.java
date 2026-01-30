package com.hcmus.course_recommendation.recommendation.fs.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.course.model.CourseDataset;
import com.hcmus.course_recommendation.recommendation.fs.dto.FSRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.fs.dto.FSRefinedRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.fs.dto.ServerFSRecommendationResult;
import com.hcmus.course_recommendation.recommendation.fs.service.FSService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FSController {
	private final FSService fsService;

	@PostMapping("/fs/recommendation")
	public RestResponse<ServerFSRecommendationResult> getFeatureSentimentRecommendation(
		@RequestBody FSRecommendationRequest request, Principal principal) {
		request.setUserId(principal.getName());
		return RestResponse.make(fsService.getFSRecommendation(request));
	}

	@PostMapping("/fs/recommendation/refined")
	public RestResponse<ServerFSRecommendationResult> getFeatureSentimentRefinedRecommendation(
		@RequestBody FSRefinedRecommendationRequest request, Principal principal) {
		request.setUserId(principal.getName());
		return RestResponse.make(fsService.getFSRefinedRecommendation(request));
	}

	@GetMapping("/fs/attributes")
	public RestResponse<List<String>> getAttributes(@RequestParam CourseDataset dataset) {
		return RestResponse.make(fsService.getAttributeValues(dataset));
	}

	@GetMapping("/fs/attribute-value-to-label")
	public RestResponse<Map<String, String>> getAttributeValueToLabel(@RequestParam CourseDataset dataset) {
		return RestResponse.make(fsService.getAttributeValueToLabel(dataset));
	}

	@GetMapping("/fs/user-preference")
	public RestResponse<Map<String, Double>> getAttributeToTargetSentimentScore(@RequestParam CourseDataset dataset,
		Principal principal) {
		return RestResponse.make(fsService.getAttributeToTargetSentimentScore(dataset, principal.getName()));
	}

	@GetMapping("/fs/latest-recommendation")
	public RestResponse<ServerFSRecommendationResult> getLatestRecommendationResult(@RequestParam CourseDataset dataset,
		Principal principal) {
		return RestResponse.make(fsService.getLatestRecommendationResult(dataset, principal.getName()));
	}
}

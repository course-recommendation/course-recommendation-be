package com.hcmus.course_recommendation.recommendation;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Dataset;

import lombok.RequiredArgsConstructor;

@RestController("/recommendation")
@RequiredArgsConstructor
public class RecommendationController {
	private final RecommendationService recommendationService;

	@GetMapping("/attributes")
	public RestResponse<List<String>> getAttributeValues(@RequestParam Dataset dataset,
		@RequestParam Algorithm algorithm) {
		return RestResponse.make(recommendationService.getAttributeValues(dataset, algorithm));
	}

	@GetMapping("/user-preference")
	public RestResponse<Map<String, Double>> getAttributeToTargetSentimentScore(@RequestParam Dataset dataset,
		@RequestParam Algorithm algorithm,
		Principal principal) {
		return RestResponse.make(
			recommendationService.getAttributeValueToScore(dataset, algorithm, principal.getName()));
	}
}

package com.hcmus.course_recommendation.recommendation;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.recommendation.model.Attribute;
import com.hcmus.course_recommendation.tenant.TenantId;

import lombok.RequiredArgsConstructor;

@RestController("/recommendation")
@RequiredArgsConstructor
public class RecommendationController {
	private final RecommendationService recommendationService;

	@GetMapping("/attributes")
	public RestResponse<List<Attribute>> getAttributes(@RequestParam Algorithm algorithm, @TenantId Long tenantId) {
		return RestResponse.make(recommendationService.getAttributes(algorithm, tenantId));
	}

	@GetMapping("/user-preference")
	public RestResponse<Map<String, Double>> getAttributeValueToScore(
		@RequestParam Algorithm algorithm, @TenantId Long tenantId,
		Principal principal) {
		return RestResponse.make(
			recommendationService.getAttributeValueToScore(algorithm, tenantId, principal.getName()));
	}
}

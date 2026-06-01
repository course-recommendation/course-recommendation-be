package com.hcmus.course_recommendation.recommendation;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.recommendation.fs.repository.UserPreferenceRepository;
import com.hcmus.course_recommendation.recommendation.model.Attribute;
import com.hcmus.course_recommendation.recommendation.repository.AttributeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecommendationService {
	private final AttributeRepository attributeRepository;
	private final UserPreferenceRepository fSUserPreferenceRepository;

	@Transactional(readOnly = true)
	public List<String> getAttributeValues(Algorithm algorithm, Long tenantId) {
		return attributeRepository.findByAlgorithmAndTenantId(algorithm, tenantId)
			.stream()
			.map(Attribute::getValue)
			.toList();
	}

	@Transactional(readOnly = true)
	public Map<String, Double> getAttributeValueToScore(Algorithm algorithm, Long tenantId, String userId) {
		return fSUserPreferenceRepository.findByAlgorithmAndTenantIdAndUserId(algorithm, tenantId, userId)
			.map(x -> x.getData().attributeToScore())
			.orElse(null);
	}
}

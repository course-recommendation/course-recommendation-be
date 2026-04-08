package com.hcmus.course_recommendation.recommendation;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.recommendation.fs.repository.UserPreferenceRepository;
import com.hcmus.course_recommendation.recommendation.model.Attribute;
import com.hcmus.course_recommendation.recommendation.reposiroty.AttributeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecommendationService {
	private final AttributeRepository attributeRepository;
	private final UserPreferenceRepository fSUserPreferenceRepository;

	@Transactional(readOnly = true)
	public List<String> getAttributeValues(Algorithm algorithm) {
		return attributeRepository.findByAlgorithm(algorithm)
			.stream()
			.map(Attribute::getValue)
			.toList();
	}

	@Transactional(readOnly = true)
	public Map<String, Double> getAttributeValueToScore(Algorithm algorithm, String userId) {
		return fSUserPreferenceRepository.findByAlgorithmAndUserId(algorithm, userId)
			.map(x -> x.getData().attributeToScore())
			.orElse(null);
	}
}

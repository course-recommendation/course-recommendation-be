package com.hcmus.course_recommendation.recommendation;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Dataset;
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
	public List<String> getAttributeValues(Dataset dataset, Algorithm algorithm) {
		return attributeRepository.findByDatasetAndAlgorithm(dataset, algorithm)
			.stream()
			.map(Attribute::getValue)
			.toList();
	}

	@Transactional(readOnly = true)
	public Map<String, Double> getAttributeValueToScore(Dataset dataset, Algorithm algorithm, String userId) {
		return fSUserPreferenceRepository.findByDatasetAndAlgorithmAndUserId(dataset, algorithm, userId)
			.map(x -> x.getData().attributeToScore())
			.orElse(null);
	}
}

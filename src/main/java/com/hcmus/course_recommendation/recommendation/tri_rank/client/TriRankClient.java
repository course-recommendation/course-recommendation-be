package com.hcmus.course_recommendation.recommendation.tri_rank.client;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.hcmus.course_recommendation.recommendation.tri_rank.client.dto.ClientTriRankRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.tri_rank.client.dto.ClientTriRankRecommendationResult;
import com.hcmus.course_recommendation.recommendation.tri_rank.client.dto.ClientTriRankTrainRequest;

@HttpExchange
@Component
public interface TriRankClient {
	@PostExchange("/trirank/recommendation")
	ClientTriRankRecommendationResult getRecommendation(@RequestBody ClientTriRankRecommendationRequest request);

	@PostExchange("/trirank/train")
	void train(@RequestBody ClientTriRankTrainRequest request);
}


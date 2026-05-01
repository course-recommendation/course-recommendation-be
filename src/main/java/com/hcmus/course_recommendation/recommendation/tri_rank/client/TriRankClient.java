package com.hcmus.course_recommendation.recommendation.tri_rank.client;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.hcmus.course_recommendation.recommendation.tri_rank.client.dto.ClientTriRankRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.tri_rank.client.dto.ClientTriRankRecommendationResult;

@HttpExchange
@Component
public interface TriRankClient {
	@PostExchange("/trirank/recommendation")
	ClientTriRankRecommendationResult getRecommendation(@RequestBody ClientTriRankRecommendationRequest request);

	@GetExchange("/trirank/topk-aspect-of-item")
	List<List<Object>> getTopKAspectOfItem(@RequestParam("item_id") String itemId,
		@RequestParam("k") Long k);
}


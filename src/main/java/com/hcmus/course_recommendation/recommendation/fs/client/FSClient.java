package com.hcmus.course_recommendation.recommendation.fs.client;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSExtractSentimentsRequest;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSExtractSentimentsResult;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSRecommendationResult;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSRefinedRecommendationRequest;

@HttpExchange
@Component
public interface FSClient {
	@PostExchange("/fs/recommendation")
	ClientFSRecommendationResult getRecommendation(@RequestBody ClientFSRecommendationRequest request);

	@PostExchange("/fs/recommendation/refined")
	ClientFSRecommendationResult getRefinedRecommendation(@RequestBody ClientFSRefinedRecommendationRequest request);

	@PostExchange("/fs/extract-sentiments")
	List<ClientFSExtractSentimentsResult> getSentiments(@RequestBody ClientFSExtractSentimentsRequest request);
}

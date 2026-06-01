package com.hcmus.course_recommendation.recommendation.fs.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSTradeoffPair;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FSRefinedRecommendationRequest {
	private Long recommendationId;
	private String itemId;
	private List<ClientFSTradeoffPair> category;
	@JsonIgnore
	private String userId;
	@JsonIgnore
	private Long tenantId;
}

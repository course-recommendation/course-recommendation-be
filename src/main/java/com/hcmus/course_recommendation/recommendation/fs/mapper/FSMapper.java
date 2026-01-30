package com.hcmus.course_recommendation.recommendation.fs.mapper;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;

import com.hcmus.course_recommendation.course.model.FSItemSentiment;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSItemSentiment;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSPreferenceConfigure;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSRecommendationResult;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSTradeoffPair;
import com.hcmus.course_recommendation.recommendation.fs.model.FSPreferenceConfigure;
import com.hcmus.course_recommendation.recommendation.fs.model.FSRecommendationResultData;
import com.hcmus.course_recommendation.recommendation.fs.model.FSTradeoffPair;

@Mapper
public interface FSMapper {
	FSRecommendationResultData toFeatureSentimentRecommendationResultData(ClientFSRecommendationResult source);

	Map<String, ClientFSPreferenceConfigure> toStringToClientFSPreferenceConfigure(
		Map<String, FSPreferenceConfigure> source);

	Map<String, List<FSTradeoffPair>> toStringToFSTradeoffPairs(Map<String, List<ClientFSTradeoffPair>> source);

	List<FSTradeoffPair> toFSTradeoffPairs(List<ClientFSTradeoffPair> source);

	List<ClientFSTradeoffPair> toClientFSTradeoffPairs(List<FSTradeoffPair> source);

	Map<String, List<ClientFSItemSentiment>> toStringToClientFSItemSentiments(
		Map<String, List<FSItemSentiment>> source);

	List<ClientFSItemSentiment> toClientFSItemSentiments(List<FSItemSentiment> source);
}

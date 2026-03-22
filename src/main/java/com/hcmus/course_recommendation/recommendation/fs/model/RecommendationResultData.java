package com.hcmus.course_recommendation.recommendation.fs.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hcmus.course_recommendation.course.model.Algorithm;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "algorithm"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = FsRecommendationResultData.class, name = "FS"),
})
public interface RecommendationResultData {

	@JsonIgnore
	Algorithm getAlgorithm();
}

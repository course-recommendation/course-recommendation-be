package com.hcmus.course_recommendation.recommendation.fs.client.dto;

import lombok.Builder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ClientFSPreferenceConfigure(Double weight, Double targetSentimentScore) {
}

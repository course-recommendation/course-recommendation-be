package com.hcmus.course_recommendation.recommendation.fs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FSPreferenceConfigure {
	private Double weight;
	private Double targetSentimentScore;
}

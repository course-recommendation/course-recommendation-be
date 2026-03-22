package com.hcmus.course_recommendation.recommendation.fs.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Dataset;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RecommendationResult {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private Dataset dataset;

	@Enumerated(EnumType.STRING)
	private Algorithm algorithm;

	private String userId;

	@JdbcTypeCode(SqlTypes.JSON)
	private FsRecommendationResultData data;
}

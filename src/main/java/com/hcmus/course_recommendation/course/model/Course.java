package com.hcmus.course_recommendation.course.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Course {
	@Id
	private String id;

	@Enumerated(EnumType.STRING)
	@JsonIgnore
	private CourseAlgorithm algorithm;

	@Enumerated(EnumType.STRING)
	private CourseDataset dataset;

	private String name;

	@JdbcTypeCode(SqlTypes.JSON)
	@JsonIgnore
	private CourseExtraData extraData;
}

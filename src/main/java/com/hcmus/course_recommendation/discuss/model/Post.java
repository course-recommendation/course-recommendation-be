package com.hcmus.course_recommendation.discuss.model;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Dataset;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Enumerated(EnumType.STRING)
	private Dataset dataset;
	@Enumerated(EnumType.STRING)
	private Algorithm algorithm;
	private String userId;
	private String content;
	private String courseCode;
	@CreationTimestamp
	private Instant createdAt;
}

package com.hcmus.course_recommendation.course.dto;

import com.hcmus.course_recommendation.course.model.CourseAlgorithm;
import com.hcmus.course_recommendation.course.model.CourseDataset;

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
public class CourseDomain {
	private CourseAlgorithm algorithm;
	private CourseDataset dataset;
}

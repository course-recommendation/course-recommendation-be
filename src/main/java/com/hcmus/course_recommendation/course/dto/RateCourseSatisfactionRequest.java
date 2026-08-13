package com.hcmus.course_recommendation.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Overall 1-5 star satisfaction with a course. Kept separate from {@link RateCourseRequest} because
 * the rating popover lets a student change the overall star and an individual attribute independently,
 * and each edit is saved on its own.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RateCourseSatisfactionRequest {
	private Integer score;
}

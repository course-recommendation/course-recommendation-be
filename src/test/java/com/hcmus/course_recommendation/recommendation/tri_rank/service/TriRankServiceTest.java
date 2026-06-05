package com.hcmus.course_recommendation.recommendation.tri_rank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hcmus.course_recommendation.course.model.UserCourseRating;

class TriRankServiceTest {

	private final TriRankService triRankService = new TriRankService(null, null, null);

	@Test
	void buildRatingFileContentShouldAverageScoresPerUserAndCourse() {
		var courseIdToCourseCode = Map.of(1L, "B008DJIGR4", 2L, "B0094AHQ6S");
		var userCourseRatings = List.of(
			UserCourseRating.builder().userId("A00900752UZ2JAC47K2RR").courseId(1L).attributeId(1L).score(4).build(),
			UserCourseRating.builder().userId("A00900752UZ2JAC47K2RR").courseId(1L).attributeId(2L).score(2).build(),
			UserCourseRating.builder().userId("A00900752UZ2JAC47K2RR").courseId(2L).attributeId(3L).score(3).build(),
			UserCourseRating.builder().userId("B_USER").courseId(1L).attributeId(3L).score(5).build()
		);

		var content = triRankService.buildRatingFileContent(userCourseRatings, courseIdToCourseCode);

		assertEquals(String.join("\n",
			"A00900752UZ2JAC47K2RR,B008DJIGR4,3.000000,1400630400",
			"A00900752UZ2JAC47K2RR,B0094AHQ6S,3.000000,1400630400",
			"B_USER,B008DJIGR4,5.000000,1400630400"), content);
	}

	@Test
	void buildSentimentFileContentShouldIncludeOnlyScoresAtLeastThree() {
		var courseIdToCourseCode = Map.of(1L, "B008DJIGR4", 2L, "B0094AHQ6S");
		var userCourseRatings = List.of(
			UserCourseRating.builder().userId("A00900752UZ2JAC47K2RR").courseId(1L).attributeId(1L).score(4).build(),
			UserCourseRating.builder().userId("A00900752UZ2JAC47K2RR").courseId(1L).attributeId(2L).score(2).build(),
			UserCourseRating.builder().userId("A00900752UZ2JAC47K2RR").courseId(2L).attributeId(3L).score(3).build(),
			UserCourseRating.builder().userId("B_USER").courseId(1L).attributeId(3L).score(5).build()
		);
		var attributeIdToValue = Map.of(1L, "theory", 2L, "homework", 3L, "screen");

		var content = triRankService.buildSentimentFileContent(userCourseRatings, courseIdToCourseCode, attributeIdToValue);

		assertEquals(String.join("\n",
			"A00900752UZ2JAC47K2RR,B008DJIGR4,theory:good:1",
			"A00900752UZ2JAC47K2RR,B0094AHQ6S,screen:good:1",
			"B_USER,B008DJIGR4,screen:good:1"), content);
	}
}



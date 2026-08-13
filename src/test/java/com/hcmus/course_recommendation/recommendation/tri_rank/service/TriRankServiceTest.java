package com.hcmus.course_recommendation.recommendation.tri_rank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hcmus.course_recommendation.course.model.UserCourseRating;

class TriRankServiceTest {

	private final TriRankService triRankService = new TriRankService(null, null, null, null, null);

	private static final Map<Long, String> COURSE_ID_TO_CODE = Map.of(1L, "B008DJIGR4", 2L, "B0094AHQ6S");
	private static final Map<Long, String> ATTRIBUTE_ID_TO_VALUE = Map.of(1L, "theory", 2L, "homework", 3L, "screen");

	private static UserCourseRating rating(String userId, long courseId, long attributeId, int score) {
		return UserCourseRating.builder()
			.userId(userId)
			.courseId(courseId)
			.attributeId(attributeId)
			.score(score)
			.build();
	}

	@Test
	void buildRatingFileContentShouldUseTheOverallSatisfactionScore() {
		var userCourseRatings = List.of(
			rating("USER_A", 1L, 1L, 4),
			rating("USER_A", 1L, 2L, 2),
			rating("USER_A", 2L, 3L, 3),
			rating("USER_B", 1L, 3L, 5));
		var satisfaction = Map.of(
			new TriRankService.UserCourseKey("USER_A", "B008DJIGR4"), 4.5,
			new TriRankService.UserCourseKey("USER_A", "B0094AHQ6S"), 1.25,
			new TriRankService.UserCourseKey("USER_B", "B008DJIGR4"), 2.0);

		var content = triRankService.buildRatingFileContent(userCourseRatings, COURSE_ID_TO_CODE, satisfaction);

		assertEquals(String.join("\n",
			"USER_A,B008DJIGR4,4.500000,1400630400",
			"USER_A,B0094AHQ6S,1.250000,1400630400",
			"USER_B,B008DJIGR4,2.000000,1400630400"), content);
	}

	@Test
	void buildRatingFileContentShouldFallBackToTheAttributeAverageWhenSatisfactionIsMissing() {
		var userCourseRatings = List.of(
			rating("USER_A", 1L, 1L, 4),
			rating("USER_A", 1L, 2L, 2));

		var content = triRankService.buildRatingFileContent(userCourseRatings, COURSE_ID_TO_CODE, Map.of());

		assertEquals("USER_A,B008DJIGR4,3.000000,1400630400", content);
	}

	/**
	 * With a third of the mass at each end, the terciles land on 2 and 4, so a 1 or 2 is the low pole,
	 * a 4 or 5 the high pole, and a 3 leans neither way and emits nothing.
	 */
	@Test
	void buildSentimentFileContentShouldEmitOneAspectPerPole() {
		var userCourseRatings = List.of(
			rating("USER_A", 1L, 1L, 5),
			rating("USER_A", 1L, 2L, 1),
			rating("USER_A", 1L, 3L, 3),
			rating("USER_B", 1L, 1L, 1),
			rating("USER_B", 1L, 2L, 5),
			rating("USER_B", 1L, 3L, 3),
			rating("USER_C", 2L, 1L, 3),
			rating("USER_C", 2L, 2L, 3),
			rating("USER_C", 2L, 3L, 3));

		var content = triRankService.buildSentimentFileContent(userCourseRatings, COURSE_ID_TO_CODE,
			ATTRIBUTE_ID_TO_VALUE);

		// USER_C rated everything at the midpoint, so that review contributes no aspect line at all.
		assertEquals(String.join("\n",
			"USER_A,B008DJIGR4,theory@high:mentioned:1,homework@low:mentioned:1",
			"USER_B,B008DJIGR4,theory@low:mentioned:1,homework@high:mentioned:1"), content);
	}

	/** An attribute every student scored identically carries no information, so it earns no edges. */
	@Test
	void buildSentimentFileContentShouldEmitNothingForAConstantAttribute() {
		var userCourseRatings = List.of(
			rating("USER_A", 1L, 1L, 3),
			rating("USER_B", 1L, 1L, 3),
			rating("USER_C", 2L, 1L, 3));

		var content = triRankService.buildSentimentFileContent(userCourseRatings, COURSE_ID_TO_CODE,
			ATTRIBUTE_ID_TO_VALUE);

		assertEquals("", content);
	}

	/**
	 * The cut is derived per attribute rather than hard-coded, so a skewed attribute still splits into
	 * two balanced poles. Here 4 is the most common score, which pushes the low cut up to 4 and leaves
	 * only 5 as the high pole.
	 */
	@Test
	void buildSentimentFileContentShouldDeriveTheCutFromEachAttributesOwnSpread() {
		var userCourseRatings = List.of(
			rating("USER_A", 1L, 1L, 4),
			rating("USER_B", 1L, 1L, 4),
			rating("USER_C", 1L, 1L, 5));

		var content = triRankService.buildSentimentFileContent(userCourseRatings, COURSE_ID_TO_CODE,
			ATTRIBUTE_ID_TO_VALUE);

		assertEquals(String.join("\n",
			"USER_A,B008DJIGR4,theory@low:mentioned:1",
			"USER_B,B008DJIGR4,theory@low:mentioned:1",
			"USER_C,B008DJIGR4,theory@high:mentioned:1"), content);
	}
}

package com.hcmus.course_recommendation.recommendation.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Asserts the statistical properties that the old generator lacked. These are the properties that
 * decide whether a personalised, scrutable recommender can be evaluated on the dataset at all, so
 * they are worth pinning down rather than eyeballing.
 *
 * <p>Sizes match the real TriRank tenant (201 users, 179 courses, 7 bipolar attributes).
 */
class SyntheticRatingGeneratorTest {

	private static final int USERS = 201;
	private static final int COURSES = 179;
	private static final int ATTRIBUTES = 7;
	private static final long SEED = 123L;

	private final SyntheticRatingGenerator.Dataset dataset =
		SyntheticRatingGenerator.generate(USERS, COURSES, ATTRIBUTES, SEED);

	@Test
	void everyScoreShouldStayOnTheOneToFiveScale() {
		for (var rating : dataset.attributeRatings()) {
			assertTrue(rating.score() >= SyntheticRatingGenerator.MIN_SCORE
					&& rating.score() <= SyntheticRatingGenerator.MAX_SCORE,
				"attribute score out of range: " + rating.score());
		}
		for (var satisfaction : dataset.satisfactions()) {
			assertTrue(satisfaction.score() >= SyntheticRatingGenerator.MIN_SCORE
					&& satisfaction.score() <= SyntheticRatingGenerator.MAX_SCORE,
				"satisfaction out of range: " + satisfaction.score());
		}
	}

	/**
	 * The whole point of the rewrite. A user's latent taste has to show up in the data, and it can
	 * only do so through which courses they chose - the per-attribute ratings themselves are
	 * deliberately course-driven. On the old generator this correlation was ~0.
	 */
	@Test
	void aUsersLatentTasteShouldBeRecoverableFromTheirRatings() {
		double[][] sum = new double[USERS][ATTRIBUTES];
		int[][] count = new int[USERS][ATTRIBUTES];
		for (var rating : dataset.attributeRatings()) {
			sum[rating.userIndex()][rating.attributeIndex()] += rating.score();
			count[rating.userIndex()][rating.attributeIndex()]++;
		}

		List<double[]> pairs = new ArrayList<>();
		for (int user = 0; user < USERS; user++) {
			for (int attribute = 0; attribute < ATTRIBUTES; attribute++) {
				if (count[user][attribute] > 0) {
					pairs.add(new double[] {dataset.userTaste()[user][attribute],
						sum[user][attribute] / count[user][attribute]});
				}
			}
		}
		double r = pearson(pairs);
		assertTrue(r > 0.45, "taste should be recoverable from the ratings, but r = " + r);
	}

	/** Course profiles share latent factors, so the axes must correlate rather than be independent. */
	@Test
	void theBipolarAxesShouldCorrelateAcrossCourses() {
		double maxAbsCorrelation = 0;
		for (int a = 0; a < ATTRIBUTES; a++) {
			for (int b = a + 1; b < ATTRIBUTES; b++) {
				List<double[]> pairs = new ArrayList<>();
				for (int course = 0; course < COURSES; course++) {
					pairs.add(new double[] {dataset.courseProfile()[course][a], dataset.courseProfile()[course][b]});
				}
				maxAbsCorrelation = Math.max(maxAbsCorrelation, Math.abs(pearson(pairs)));
			}
		}
		// The 95% noise band at n=179 is about +/-0.15, so anything past 0.25 is real structure.
		assertTrue(maxAbsCorrelation > 0.25,
			"axes should share latent structure, but the strongest |r| was " + maxAbsCorrelation);
	}

	/**
	 * Satisfaction has to carry signal, unlike the mean of the seven bipolar axes it replaces (which
	 * measured SD 0.49 with 67% of values inside [2.5, 3.5]).
	 */
	@Test
	void satisfactionShouldSpreadAndTrackTasteMismatch() {
		List<double[]> pairs = new ArrayList<>();
		double sum = 0;
		for (var satisfaction : dataset.satisfactions()) {
			double mismatch = 0;
			for (int attribute = 0; attribute < ATTRIBUTES; attribute++) {
				mismatch += Math.abs(dataset.userTaste()[satisfaction.userIndex()][attribute]
					- dataset.courseProfile()[satisfaction.courseIndex()][attribute]);
			}
			pairs.add(new double[] {mismatch / ATTRIBUTES, satisfaction.score()});
			sum += satisfaction.score();
		}

		double mean = sum / dataset.satisfactions().size();
		double variance = dataset.satisfactions().stream()
			.mapToDouble(satisfaction -> Math.pow(satisfaction.score() - mean, 2))
			.sum() / (dataset.satisfactions().size() - 1);
		double sd = Math.sqrt(variance);
		assertTrue(sd > 0.5, "satisfaction should spread, but SD = " + sd);

		double r = pearson(pairs);
		assertTrue(r < -0.5, "satisfaction should fall as taste mismatch grows, but r = " + r);
	}

	/** Real datasets are long-tailed in both user activity and item popularity; a flat 20 is not. */
	@Test
	void activityAndPopularityShouldBeLongTailed() {
		Map<Integer, Integer> coursesPerUser = new HashMap<>();
		Map<Integer, Integer> usersPerCourse = new HashMap<>();
		for (var satisfaction : dataset.satisfactions()) {
			coursesPerUser.merge(satisfaction.userIndex(), 1, Integer::sum);
			usersPerCourse.merge(satisfaction.courseIndex(), 1, Integer::sum);
		}

		int minCourses = coursesPerUser.values().stream().mapToInt(Integer::intValue).min().orElseThrow();
		int maxCourses = coursesPerUser.values().stream().mapToInt(Integer::intValue).max().orElseThrow();
		assertTrue(maxCourses >= 2 * minCourses,
			"user activity should be long-tailed, but ranged " + minCourses + ".." + maxCourses);

		int minRaters = usersPerCourse.values().stream().mapToInt(Integer::intValue).min().orElseThrow();
		int maxRaters = usersPerCourse.values().stream().mapToInt(Integer::intValue).max().orElseThrow();
		assertTrue(maxRaters >= 3 * minRaters,
			"course popularity should be long-tailed, but ranged " + minRaters + ".." + maxRaters);
	}

	@Test
	void generationShouldBeReproducibleForAGivenSeed() {
		var again = SyntheticRatingGenerator.generate(USERS, COURSES, ATTRIBUTES, SEED);
		assertEquals(dataset.attributeRatings(), again.attributeRatings());
		assertEquals(dataset.satisfactions(), again.satisfactions());
	}

	private static double pearson(List<double[]> pairs) {
		int n = pairs.size();
		double sumX = 0;
		double sumY = 0;
		double sumXy = 0;
		double sumXx = 0;
		double sumYy = 0;
		for (double[] pair : pairs) {
			sumX += pair[0];
			sumY += pair[1];
			sumXy += pair[0] * pair[1];
			sumXx += pair[0] * pair[0];
			sumYy += pair[1] * pair[1];
		}
		return (n * sumXy - sumX * sumY)
			/ (Math.sqrt(n * sumXx - sumX * sumX) * Math.sqrt(n * sumYy - sumY * sumY));
	}
}

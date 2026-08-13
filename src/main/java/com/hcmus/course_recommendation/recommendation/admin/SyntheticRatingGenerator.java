package com.hcmus.course_recommendation.recommendation.admin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Database-free generator for the synthetic demo/experiment dataset.
 *
 * <p>The previous generator drew one independent target per (course, attribute) from U(1,5) and then
 * handed every user 20 uniformly random courses. That produced a dataset with no user-level
 * structure whatsoever: measured on the generated data, the between-user spread of a user's mean
 * score per attribute was 0.29, exactly the 0.31 predicted when users have zero taste and differ
 * only in which random courses they happened to receive. On such data the optimal recommender is a
 * non-personalised one, so neither personalisation nor aspect scrutability can be evaluated at all.
 *
 * <p>This generator keeps the one thing the old one got right and fixes the rest:
 *
 * <ul>
 * <li><b>Attribute ratings stay course-driven.</b> The attributes are descriptive bipolar axes
 * ("Lý thuyết ↔ Thực hành"); where a course sits on an axis is a property of the course, so every
 * student should agree up to perception noise. There is deliberately no per-user term here.
 * <li><b>Users get a latent taste, and taste drives which courses they take.</b> This is where
 * preference belongs, and it is the signal a recommender is supposed to exploit. Selection is
 * Plackett-Luce (Gumbel top-k) over a taste-match score, so the dataset is missing-not-at-random
 * the way real course enrolment is.
 * <li><b>Course profiles share latent factors</b> instead of being independent per axis, so the
 * axes correlate the way real courses do (a practical course also tends to be applied and modern)
 * and there is low-rank structure for aspect filtering to exploit.
 * <li><b>Activity and popularity are long-tailed</b> rather than a flat 20 courses for everyone.
 * <li><b>An explicit satisfaction score is produced</b> for the user-item matrix R. Averaging the
 * seven bipolar axes (the old behaviour) is pinned near 3 by the central limit theorem - measured
 * SD 0.49 with 67% of all values inside [2.5, 3.5] - so R carried almost no signal.
 * </ul>
 *
 * <p>Everything is a pure function of {@code seed}, so a run is reproducible.
 */
final class SyntheticRatingGenerator {

	static final int MIN_SCORE = 1;
	static final int MAX_SCORE = 5;
	/** Midpoint of the bipolar scale: neither pole. */
	static final double NEUTRAL_SCORE = 3.0;

	/**
	 * Number of latent factors shared by the course profiles, which is what correlates the axes. Too
	 * few and pairs of axes become near-duplicates (at 3 factors the strongest pair reached r=0.85,
	 * which no real pair of course attributes does).
	 */
	private static final int LATENT_FACTORS = 5;
	private static final double PROFILE_FACTOR_SPREAD = 1.00;
	private static final double PROFILE_NOISE_SIGMA = 0.55;
	/** Per-student disagreement about where a course sits on an axis. */
	private static final double PERCEPTION_NOISE_SIGMA = 0.80;

	/** Taste archetypes give users overlapping tastes, which is what makes collaborative filtering work. */
	private static final int TASTE_ARCHETYPES = 6;
	private static final double TASTE_JITTER_SIGMA = 0.55;

	private static final int COURSES_PER_USER_MIN = 10;
	private static final int COURSES_PER_USER_MAX = 45;
	/** >1 skews activity towards the low end, producing the long tail real datasets have. */
	private static final double COURSES_PER_USER_SKEW = 2.2;

	/** Kept moderate so no course ends up with too few raters for its aspect profile to be estimable. */
	private static final double POPULARITY_SIGMA = 0.45;
	/** Lower means taste dominates course choice; higher means choice is closer to random. */
	private static final double SELECTION_TEMPERATURE = 0.38;

	private static final double SATISFACTION_INTERCEPT = 5.6;
	private static final double SATISFACTION_MISMATCH_SLOPE = 2.0;
	private static final double SATISFACTION_NOISE_SIGMA = 0.50;

	private SyntheticRatingGenerator() {
	}

	/** One generated rating of a single attribute of a single course, by index. */
	record AttributeRating(int userIndex, int courseIndex, int attributeIndex, int score) {
	}

	/** One generated overall-satisfaction rating of a course, by index. */
	record Satisfaction(int userIndex, int courseIndex, double score) {
	}

	/**
	 * @param attributeRatings per-attribute descriptive ratings
	 * @param satisfactions overall satisfaction per (user, course), the intended source for R
	 * @param userTaste [userIndex][attributeIndex] latent target on each bipolar axis; persisting
	 *     this as the user's stated preference is what gives the scrutability feature a ground truth
	 * @param courseProfile [courseIndex][attributeIndex] true position of each course on each axis
	 */
	record Dataset(
		List<AttributeRating> attributeRatings,
		List<Satisfaction> satisfactions,
		double[][] userTaste,
		double[][] courseProfile
	) {
	}

	static Dataset generate(int numUsers, int numCourses, int numAttributes, Long seed) {
		Random random = seed != null ? new Random(seed) : new Random();

		double[][] courseProfile = buildCourseProfiles(random, numCourses, numAttributes);
		double[][] userTaste = buildUserTastes(random, numUsers, numAttributes);

		// Long-tailed course popularity, so a few courses are taken far more often than the rest.
		double[] logPopularity = new double[numCourses];
		for (int course = 0; course < numCourses; course++) {
			logPopularity[course] = random.nextGaussian() * POPULARITY_SIGMA;
		}

		List<AttributeRating> attributeRatings = new ArrayList<>();
		List<Satisfaction> satisfactions = new ArrayList<>();
		for (int user = 0; user < numUsers; user++) {
			int courseCount = drawCourseCount(random, numCourses);
			for (int course : selectCourses(random, courseCount, numCourses, userTaste[user], courseProfile,
				logPopularity)) {
				for (int attribute = 0; attribute < numAttributes; attribute++) {
					double perceived = courseProfile[course][attribute] + random.nextGaussian() * PERCEPTION_NOISE_SIGMA;
					attributeRatings.add(new AttributeRating(user, course, attribute,
						Math.clamp(Math.round(perceived), MIN_SCORE, MAX_SCORE)));
				}
				double satisfaction = SATISFACTION_INTERCEPT
					- SATISFACTION_MISMATCH_SLOPE * mismatch(userTaste[user], courseProfile[course])
					+ random.nextGaussian() * SATISFACTION_NOISE_SIGMA;
				satisfactions.add(new Satisfaction(user, course,
					Math.clamp(satisfaction, (double)MIN_SCORE, (double)MAX_SCORE)));
			}
		}
		return new Dataset(attributeRatings, satisfactions, userTaste, courseProfile);
	}

	/**
	 * Course positions are a linear combination of a few shared latent factors, so the axes end up
	 * correlated across courses instead of independent.
	 */
	private static double[][] buildCourseProfiles(Random random, int numCourses, int numAttributes) {
		double[][] loading = new double[numAttributes][LATENT_FACTORS];
		for (int attribute = 0; attribute < numAttributes; attribute++) {
			for (int factor = 0; factor < LATENT_FACTORS; factor++) {
				loading[attribute][factor] = random.nextGaussian();
			}
		}

		double[][] profile = new double[numCourses][numAttributes];
		double[] factorScore = new double[LATENT_FACTORS];
		for (int course = 0; course < numCourses; course++) {
			for (int factor = 0; factor < LATENT_FACTORS; factor++) {
				factorScore[factor] = random.nextGaussian();
			}
			for (int attribute = 0; attribute < numAttributes; attribute++) {
				double structured = 0;
				for (int factor = 0; factor < LATENT_FACTORS; factor++) {
					structured += loading[attribute][factor] * factorScore[factor];
				}
				// Dividing by sqrt(LATENT_FACTORS) keeps the spread independent of the factor count.
				double value = NEUTRAL_SCORE
					+ PROFILE_FACTOR_SPREAD * structured / Math.sqrt(LATENT_FACTORS)
					+ random.nextGaussian() * PROFILE_NOISE_SIGMA;
				profile[course][attribute] = Math.clamp(value, (double)MIN_SCORE, (double)MAX_SCORE);
			}
		}
		return profile;
	}

	/** Each user jitters around one of a few archetypes, so tastes overlap rather than being unique. */
	private static double[][] buildUserTastes(Random random, int numUsers, int numAttributes) {
		double[][] archetype = new double[TASTE_ARCHETYPES][numAttributes];
		for (int group = 0; group < TASTE_ARCHETYPES; group++) {
			for (int attribute = 0; attribute < numAttributes; attribute++) {
				archetype[group][attribute] = MIN_SCORE + random.nextDouble() * (MAX_SCORE - MIN_SCORE);
			}
		}

		double[][] taste = new double[numUsers][numAttributes];
		for (int user = 0; user < numUsers; user++) {
			int group = random.nextInt(TASTE_ARCHETYPES);
			for (int attribute = 0; attribute < numAttributes; attribute++) {
				taste[user][attribute] = Math.clamp(
					archetype[group][attribute] + random.nextGaussian() * TASTE_JITTER_SIGMA,
					(double)MIN_SCORE, (double)MAX_SCORE);
			}
		}
		return taste;
	}

	private static int drawCourseCount(Random random, int numCourses) {
		int span = COURSES_PER_USER_MAX - COURSES_PER_USER_MIN;
		int drawn = COURSES_PER_USER_MIN
			+ (int)Math.round(span * Math.pow(random.nextDouble(), COURSES_PER_USER_SKEW));
		return Math.min(drawn, numCourses);
	}

	/**
	 * Samples {@code courseCount} distinct courses without replacement, with probability proportional
	 * to {@code exp(-mismatch / temperature) * popularity}. Implemented with the Gumbel top-k trick,
	 * which draws exactly a Plackett-Luce ordering in one pass and needs no renormalisation.
	 */
	private static int[] selectCourses(Random random, int courseCount, int numCourses, double[] taste,
		double[][] courseProfile, double[] logPopularity) {
		Integer[] candidates = new Integer[numCourses];
		double[] key = new double[numCourses];
		for (int course = 0; course < numCourses; course++) {
			candidates[course] = course;
			key[course] = -mismatch(taste, courseProfile[course]) / SELECTION_TEMPERATURE
				+ logPopularity[course]
				+ gumbel(random);
		}
		// Descending by key; the top `courseCount` are the sample.
		java.util.Arrays.sort(candidates, Comparator.comparingDouble((Integer course) -> key[course]).reversed());

		int[] selected = new int[courseCount];
		for (int i = 0; i < courseCount; i++) {
			selected[i] = candidates[i];
		}
		return selected;
	}

	/** Mean absolute distance between a user's target and a course's actual position, in score points. */
	private static double mismatch(double[] taste, double[] profile) {
		double total = 0;
		for (int attribute = 0; attribute < taste.length; attribute++) {
			total += Math.abs(taste[attribute] - profile[attribute]);
		}
		return total / taste.length;
	}

	private static double gumbel(Random random) {
		// nextDouble() can return 0, which would make the outer log infinite.
		double uniform = Math.max(random.nextDouble(), 1e-12);
		return -Math.log(-Math.log(uniform));
	}
}

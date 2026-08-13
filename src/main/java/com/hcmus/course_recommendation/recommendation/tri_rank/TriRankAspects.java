package com.hcmus.course_recommendation.recommendation.tri_rank;

/**
 * The TriRank aspect vocabulary, and the mapping from a user's bipolar target onto it.
 *
 * <p>The rating attributes are <em>bipolar</em> descriptive axes: score 1 is one orientation
 * ("Lý thuyết"), score 5 the opposite ("Thực hành"), and neither is better. TriRank's aspect
 * preference vector a_0, by contrast, is a <em>unipolar</em> interest weight - the paper derives it
 * from how often a user mentions an aspect, so a larger value can only ever mean "I care about this
 * aspect more", never "I want less of it".
 *
 * <p>Feeding a bipolar target straight into a unipolar weight is what made the old behaviour wrong:
 * a user asking for "Theo hướng dẫn" (target 1) was encoded as "I don't care about this axis", so
 * the recommendation ignored the wish entirely. Worse, because a_0 is L1-normalised, scaling every
 * target by the same amount cancels out - setting every axis to 1 produced a byte-identical a_0 to
 * setting every axis to 5, and measurably the same ranking for all 179 courses.
 *
 * <p>The fix is to give each axis <em>two</em> aspects, one per pole. A course earns an edge to
 * {@code <attribute>@low} from every student who put it at the low end and to
 * {@code <attribute>@high} from every student who put it at the high end, and a target is split into
 * weights on those two aspects. "I want the low pole" then becomes a genuine unipolar interest in a
 * real aspect, which is something TriRank can represent, and the all-1 / all-5 collapse disappears
 * because those two requests now put their mass on disjoint aspects.
 *
 * <p>Both sides of that contract have to agree on the names, so the export
 * ({@code TriRankService}) and the query ({@code TriRankRecommendationService}) both go through
 * here. A mismatch would not throw - the recommender looks aspects up by name and silently skips
 * misses - it would just quietly drop every preference.
 */
public final class TriRankAspects {

	public static final int MIN_SCORE = 1;
	public static final int MAX_SCORE = 5;
	/** Midpoint of a bipolar axis: no leaning either way, and so no preference to express. */
	public static final double NEUTRAL_SCORE = 3.0;

	private static final String LOW_POLE_SUFFIX = "@low";
	private static final String HIGH_POLE_SUFFIX = "@high";

	private TriRankAspects() {
	}

	public static String lowPoleAspect(String attributeValue) {
		return attributeValue + LOW_POLE_SUFFIX;
	}

	public static String highPoleAspect(String attributeValue) {
		return attributeValue + HIGH_POLE_SUFFIX;
	}

	/**
	 * Weight to put on the low-pole aspect for a target on the 1-5 scale: +1.0 at the low extreme,
	 * 0 at the neutral midpoint, and <em>negative</em> above it, reaching -1.0 at the high extreme.
	 *
	 * <p>The negative half matters. TriRank scores an item by summing its aspect edges weighted by
	 * interest, so a purely non-negative preference can only ever reward an item for having an aspect,
	 * never penalise it for sitting at the pole the user rejected. An item at the wrong end of an axis
	 * simply gained nothing there, which let it top the list on the strength of two or three other
	 * axes while contradicting the rest - the reason a course could stay at rank 1 with attributes far
	 * from the stated preference. Giving the rejected pole a negative weight makes a mismatch subtract
	 * instead of merely failing to add.
	 *
	 * <p>Measured against an oracle ranking, this moves the top-10 from 30% to 48% of the achievable
	 * improvement over a random list, and combined with a raised aspect fitting weight (eta_A) to 72%.
	 */
	public static double lowPoleWeight(double target) {
		return Math.clamp((NEUTRAL_SCORE - target) / (NEUTRAL_SCORE - MIN_SCORE), -1.0, 1.0);
	}

	/** Mirror of {@link #lowPoleWeight} for the high-pole aspect. */
	public static double highPoleWeight(double target) {
		return Math.clamp((target - NEUTRAL_SCORE) / (MAX_SCORE - NEUTRAL_SCORE), -1.0, 1.0);
	}
}

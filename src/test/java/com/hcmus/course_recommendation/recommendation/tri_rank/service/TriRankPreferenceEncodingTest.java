package com.hcmus.course_recommendation.recommendation.tri_rank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hcmus.course_recommendation.recommendation.tri_rank.TriRankAspects;

/**
 * Covers the encoding of a bipolar target onto TriRank's unipolar aspect weights - the half of the
 * fix that lives on the query side. Its counterpart on the export side is in
 * {@link TriRankServiceTest}; the two have to agree on the aspect names or every preference is
 * silently dropped.
 */
class TriRankPreferenceEncodingTest {

	private final TriRankRecommendationService service =
		new TriRankRecommendationService(null, null, null, null, null, null, null);

	@Test
	void theLowExtremeShouldPutFullWeightOnTheLowPoleOnly() {
		assertEquals(List.of(List.of("Mức độ thực tiễn@low", 1.0)),
			service.toPolePreferences(Map.of("Mức độ thực tiễn", 1.0)));
	}

	@Test
	void theHighExtremeShouldPutFullWeightOnTheHighPoleOnly() {
		assertEquals(List.of(List.of("Mức độ thực tiễn@high", 1.0)),
			service.toPolePreferences(Map.of("Mức độ thực tiễn", 5.0)));
	}

	/** The midpoint is the form's default; it has to mean "no preference", not "the low pole". */
	@Test
	void theNeutralMidpointShouldExpressNoPreference() {
		assertEquals(List.of(), service.toPolePreferences(Map.of("Mức độ thực tiễn", 3.0)));
	}

	@Test
	void intermediateTargetsShouldLeanPartwayTowardsOnePole() {
		assertEquals(List.of(List.of("Kiểu tư duy@low", 0.5)),
			service.toPolePreferences(Map.of("Kiểu tư duy", 2.0)));
		assertEquals(List.of(List.of("Kiểu tư duy@high", 0.5)),
			service.toPolePreferences(Map.of("Kiểu tư duy", 4.0)));
	}

	/**
	 * A missing opinion used to be coerced to 0.0, which mapped onto the same tiny weight as an
	 * explicit request for the low pole. The two have to stay distinguishable.
	 */
	@Test
	void anAbsentOpinionShouldDifferFromARequestForTheLowPole() {
		Map<String, Double> withNull = new LinkedHashMap<>();
		withNull.put("Tính cập nhật", null);

		assertEquals(List.of(), service.toPolePreferences(withNull));
		assertEquals(List.of(), service.toPolePreferences(null));
		assertNotEquals(service.toPolePreferences(withNull),
			service.toPolePreferences(Map.of("Tính cập nhật", 1.0)));
	}

	/**
	 * The regression that matters most. Because a_0 is L1-normalised, the old scalar encoding made
	 * "every axis at 1" and "every axis at 5" produce a byte-identical preference vector, and measurably
	 * the same ranking for all 179 courses. Splitting the poles puts them on disjoint aspects.
	 */
	@Test
	void oppositeExtremesShouldNoLongerCollapseOntoTheSameAspects() {
		var attributes = List.of("Mức độ thực tiễn", "Kiểu tư duy", "Tính cập nhật");
		Map<String, Double> allLow = new LinkedHashMap<>();
		Map<String, Double> allHigh = new LinkedHashMap<>();
		attributes.forEach(attribute -> {
			allLow.put(attribute, 1.0);
			allHigh.put(attribute, 5.0);
		});

		var lowPreferences = service.toPolePreferences(allLow);
		var highPreferences = service.toPolePreferences(allHigh);

		assertEquals(attributes.size(), lowPreferences.size());
		assertEquals(attributes.size(), highPreferences.size());
		// Disjoint aspect names, so no amount of rescaling can make the two requests equivalent.
		assertTrue(lowPreferences.stream().noneMatch(highPreferences::contains));
		assertTrue(lowPreferences.stream().allMatch(preference -> preference.getFirst().toString().endsWith("@low")));
		assertTrue(highPreferences.stream().allMatch(preference -> preference.getFirst().toString().endsWith("@high")));
	}

	@Test
	void weightsShouldStayWithinRangeForTargetsOffTheScale() {
		assertEquals(1.0, TriRankAspects.lowPoleWeight(0.0));
		assertEquals(0.0, TriRankAspects.highPoleWeight(0.0));
		assertEquals(0.0, TriRankAspects.lowPoleWeight(6.0));
		assertEquals(1.0, TriRankAspects.highPoleWeight(6.0));
	}
}

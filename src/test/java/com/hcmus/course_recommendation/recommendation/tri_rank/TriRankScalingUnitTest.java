package com.hcmus.course_recommendation.recommendation.tri_rank;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hcmus.course_recommendation.course.service.CourseService;
import com.hcmus.course_recommendation.recommendation.fs.repository.RecommendationResultRepository;
import com.hcmus.course_recommendation.recommendation.fs.repository.UserPreferenceRepository;
import com.hcmus.course_recommendation.recommendation.tri_rank.client.TriRankClient;
import com.hcmus.course_recommendation.recommendation.tri_rank.client.dto.ClientTriRankRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.tri_rank.client.dto.ClientTriRankRecommendationResult;
import com.hcmus.course_recommendation.recommendation.tri_rank.model.TriRankItemAspect;
import com.hcmus.course_recommendation.recommendation.tri_rank.service.TriRankRecommendationService;

class TriRankScalingUnitTest {

	private final TriRankRecommendationService service = new TriRankRecommendationService(new TriRankClient() {
		@Override
		public ClientTriRankRecommendationResult getRecommendation(ClientTriRankRecommendationRequest request) {
			return null;
		}

		@Override
		public List<List<Object>> getTopKAspectOfItem(String itemId, Long k) {
			return switch (itemId) {
				case "COURSE_A" -> List.of(
					List.of("aspect-a", 10.0),
					List.of("aspect-b", 20.0),
					List.of("aspect-c", 15.0)
				);
				case "COURSE_B" -> List.of(
					List.of("aspect-d", 2.0),
					List.of("aspect-e", 2.0)
				);
				default -> List.of();
			};
		}
	}, (RecommendationResultRepository)null, (CourseService)null, (UserPreferenceRepository)null);

	@Test
	void scaleItemAspectsShouldScaleValuesIntoRangeOneToFive() throws Exception {
		var scaledAspects = invokeScaleItemAspects(List.of(
			TriRankItemAspect.builder().aspect("aspect-a").score(10.0).build(),
			TriRankItemAspect.builder().aspect("aspect-b").score(20.0).build(),
			TriRankItemAspect.builder().aspect("aspect-c").score(15.0).build()
		));

		assertEquals(3, scaledAspects.size());
		assertEquals(1.0, scaledAspects.get(0).score(), 1e-9);
		assertEquals(5.0, scaledAspects.get(1).score(), 1e-9);
		assertEquals(3.0, scaledAspects.get(2).score(), 1e-9);
	}

	@Test
	void scaleItemAspectsShouldReturnFiveWhenAllValuesAreEqual() throws Exception {
		var scaledAspects = invokeScaleItemAspects(List.of(
			TriRankItemAspect.builder().aspect("aspect-d").score(2.0).build(),
			TriRankItemAspect.builder().aspect("aspect-e").score(2.0).build()
		));

		assertEquals(2, scaledAspects.size());
		assertEquals(5.0, scaledAspects.get(0).score(), 1e-9);
		assertEquals(5.0, scaledAspects.get(1).score(), 1e-9);
	}

	@Test
	void getItemIdToItemAspectsShouldScaleEachCourseIndependently() throws Exception {
		var itemIdToItemAspects = invokeGetItemIdToItemAspects(List.of("COURSE_A", "COURSE_A", "COURSE_B"));

		assertEquals(List.of("COURSE_A", "COURSE_B"), itemIdToItemAspects.keySet().stream().toList());
		assertEquals(List.of(1.0, 5.0, 3.0),
			itemIdToItemAspects.get("COURSE_A").stream().map(TriRankItemAspect::score).toList());
		assertEquals(List.of(5.0, 5.0),
			itemIdToItemAspects.get("COURSE_B").stream().map(TriRankItemAspect::score).toList());
	}

	@Test
	void scaleItemAspectsShouldReturnEmptyListForEmptyInput() throws Exception {
		assertEquals(List.of(), invokeScaleItemAspects(List.of()));
	}

	@SuppressWarnings("unchecked")
	private List<TriRankItemAspect> invokeScaleItemAspects(List<TriRankItemAspect> itemAspects) throws Exception {
		var method = TriRankRecommendationService.class.getDeclaredMethod("scaleItemAspects", List.class);
		method.setAccessible(true);
		return (List<TriRankItemAspect>)method.invoke(service, itemAspects);
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<TriRankItemAspect>> invokeGetItemIdToItemAspects(List<String> courseCodes) throws
		Exception {
		var method = TriRankRecommendationService.class.getDeclaredMethod("getItemIdToItemAspects", List.class);
		method.setAccessible(true);
		return (Map<String, List<TriRankItemAspect>>)method.invoke(service, courseCodes);
	}
}



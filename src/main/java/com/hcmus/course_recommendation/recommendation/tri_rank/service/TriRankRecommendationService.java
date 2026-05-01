package com.hcmus.course_recommendation.recommendation.tri_rank.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.service.CourseService;
import com.hcmus.course_recommendation.recommendation.fs.model.RecommendationResult;
import com.hcmus.course_recommendation.recommendation.fs.model.UserPreference;
import com.hcmus.course_recommendation.recommendation.fs.model.UserPreferenceData;
import com.hcmus.course_recommendation.recommendation.fs.repository.RecommendationResultRepository;
import com.hcmus.course_recommendation.recommendation.fs.repository.UserPreferenceRepository;
import com.hcmus.course_recommendation.recommendation.tri_rank.client.TriRankClient;
import com.hcmus.course_recommendation.recommendation.tri_rank.client.dto.ClientTriRankRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.tri_rank.dto.ServerTriRankRecommendationResult;
import com.hcmus.course_recommendation.recommendation.tri_rank.dto.TriRankRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.tri_rank.model.TriRankItemAspect;
import com.hcmus.course_recommendation.recommendation.tri_rank.model.TriRankRecommendationResultData;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TriRankRecommendationService {
	static final Long PAGE_SIZE_FOR_ALL_ITEMS = 10_000_000_000L;
	static final String REMOVE_SEEN = "false";

	private final TriRankClient triRankClient;
	private final RecommendationResultRepository recommendationResultRepository;
	private final CourseService courseService;
	private final UserPreferenceRepository userPreferenceRepository;

	private ServerTriRankRecommendationResult toServerTriRankRecommendationResult(Long id,
		TriRankRecommendationResultData recommendationResult, Map<String, List<TriRankItemAspect>> itemIdToItemAspects,
		String userId) {
		var courseCodes = recommendationResult.courseCodes();
		var courseIdToCourseDetail = courseService.getCourseIdToCourseDetailsByCourseIds(Algorithm.TRI_RANK,
			courseCodes,
			userId);

		var courseDetails = courseCodes.stream()
			.map(courseIdToCourseDetail::get)
			.filter(Objects::nonNull)
			.toList();

		return ServerTriRankRecommendationResult.builder()
			.id(id)
			.courseDetails(courseDetails)
			.filterCoursesOptions(recommendationResult.filterCoursesOptions())
			.customFilteredCourseCodes(recommendationResult.customFilteredCourseCodes())
			.itemIdToItemAspects(itemIdToItemAspects)
			.build();
	}

	@Transactional
	public ServerTriRankRecommendationResult getTriRankRecommendation(TriRankRecommendationRequest request) {
		var oldUserPreferenceId = userPreferenceRepository.findByAlgorithmAndUserId(Algorithm.TRI_RANK,
			request.getUserId()).map(UserPreference::getId).orElse(null);
		var newUserPreferenceData = UserPreferenceData.builder()
			.attributeToScore(request.getAttributeToScore())
			.build();
		userPreferenceRepository.save(new UserPreference(oldUserPreferenceId, Algorithm.TRI_RANK, request.getUserId(),
			newUserPreferenceData));

		var clientRequest = ClientTriRankRecommendationRequest.builder()
			.uid(request.getUserId())
			.k(courseService.countByAlgorithm(Algorithm.TRI_RANK))
			.preferences(request.getAttributeToScore()
				.entrySet()
				.stream()
				.map(entry -> List.<Object>of(entry.getKey(), Objects.requireNonNullElse(
					entry.getValue(), 0.0)))
				.toList())
			.removeSeen(REMOVE_SEEN)
			.build();

		var response = triRankClient.getRecommendation(clientRequest);
		var filteredCourseCodes = courseService.getFilteredCourseCodes(Algorithm.TRI_RANK,
			request.getUserId(), request.getFilterCoursesOptions(), request.getCustomFilteredCourseCodes());
		var recommendationCourseCodes = (response.recommendations() == null ? List.<String>of() :
			response.recommendations())
			.stream()
			.filter(courseCode -> !filteredCourseCodes.contains(courseCode))
			.distinct()
			.limit(request.getNumberOfCourses())
			.toList();
		var itemIdToItemAspects = getItemIdToItemAspects(recommendationCourseCodes);

		var data = TriRankRecommendationResultData.builder()
			.courseCodes(recommendationCourseCodes)
			.filterCoursesOptions(request.getFilterCoursesOptions())
			.customFilteredCourseCodes(request.getCustomFilteredCourseCodes())
			.build();

		var savedRecommendationResult = recommendationResultRepository.save(
			new RecommendationResult(null, Algorithm.TRI_RANK, request.getUserId(), data));

		return toServerTriRankRecommendationResult(savedRecommendationResult.getId(), data, itemIdToItemAspects,
			request.getUserId());
	}

	@Transactional(readOnly = true)
	public ServerTriRankRecommendationResult getLatestTriRankRecommendationResult(String userId) {
		var latestRecommendationResult = recommendationResultRepository.getLatestRecommendationResult(
			Algorithm.TRI_RANK,
			userId);
		return latestRecommendationResult.map(recommendationResult -> {
			var recommendationResultData = (TriRankRecommendationResultData)recommendationResult.getData();
			var itemIdToItemAspects = getItemIdToItemAspects(recommendationResultData.courseCodes());
			return toServerTriRankRecommendationResult(recommendationResult.getId(), recommendationResultData,
				itemIdToItemAspects, userId);
		}).orElse(null);
	}

	private Map<String, List<TriRankItemAspect>> getItemIdToItemAspects(List<String> courseCodes) {
		return courseCodes.stream()
			.distinct()
			.collect(Collectors.toMap(courseCode -> courseCode, this::getItemAspectsByCourseCode,
				(existing, ignored) -> existing, java.util.LinkedHashMap::new));
	}

	private List<TriRankItemAspect> getItemAspectsByCourseCode(String courseCode) {
		return toTriRankItemAspects(triRankClient.getTopKAspectOfItem(courseCode, PAGE_SIZE_FOR_ALL_ITEMS));
	}

	private List<TriRankItemAspect> toTriRankItemAspects(List<List<Object>> source) {
		if (source == null) {
			return List.of();
		}

		return source.stream()
			.map(item -> {
				if (item == null || item.size() < 2) {
					return null;
				}
				var aspect = item.getFirst();
				var score = item.get(1);
				if (aspect == null || score == null) {
					return null;
				}
				return TriRankItemAspect.builder()
					.aspect(aspect.toString())
					.score(score instanceof Number number ? number.doubleValue() : Double.parseDouble(score.toString()))
					.build();
			})
			.filter(Objects::nonNull)
			.toList();
	}
}










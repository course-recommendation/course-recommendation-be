package com.hcmus.course_recommendation.recommendation.tri_rank.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseRatingRepository;
import com.hcmus.course_recommendation.course.service.CourseService;
import com.hcmus.course_recommendation.recommendation.fs.model.RecommendationResult;
import com.hcmus.course_recommendation.recommendation.fs.model.UserPreference;
import com.hcmus.course_recommendation.recommendation.fs.model.UserPreferenceData;
import com.hcmus.course_recommendation.recommendation.fs.repository.RecommendationResultRepository;
import com.hcmus.course_recommendation.recommendation.fs.repository.UserPreferenceRepository;
import com.hcmus.course_recommendation.recommendation.model.Attribute;
import com.hcmus.course_recommendation.recommendation.repository.AttributeRepository;
import com.hcmus.course_recommendation.recommendation.tri_rank.TriRankAspects;
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
	static final String REMOVE_SEEN = "false";
	/**
	 * Pseudo-ratings pulling an attribute's displayed score towards the neutral midpoint, so a course
	 * only a handful of students rated does not appear to sit firmly at one pole. Courses vary widely
	 * in how many raters they have (3 to 73 on the generated dataset), and with a per-rating spread of
	 * about 0.8 points a three-rater average carries a standard error near 0.46.
	 */
	static final double EXPLANATION_PRIOR_RATING_COUNT = 5.0;

	private final TriRankClient triRankClient;
	private final RecommendationResultRepository recommendationResultRepository;
	private final CourseService courseService;
	private final UserPreferenceRepository userPreferenceRepository;
	private final CourseRepository courseRepository;
	private final AttributeRepository attributeRepository;
	private final UserCourseRatingRepository userCourseRatingRepository;

	private ServerTriRankRecommendationResult toServerTriRankRecommendationResult(Long id,
		TriRankRecommendationResultData recommendationResult, Map<String, List<TriRankItemAspect>> itemIdToItemAspects,
		String userId, Long tenantId) {
		var courseCodes = recommendationResult.courseCodes();
		var courseIdToCourseDetail = courseService.getCourseIdToCourseDetailsByCourseIds(Algorithm.TRI_RANK,
			tenantId, courseCodes, userId);

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
		var oldUserPreferenceId = userPreferenceRepository.findByAlgorithmAndTenantIdAndUserId(Algorithm.TRI_RANK,
			request.getTenantId(), request.getUserId()).map(UserPreference::getId).orElse(null);
		var newUserPreferenceData = UserPreferenceData.builder()
			.attributeToScore(request.getAttributeToScore())
			.build();
		userPreferenceRepository.save(new UserPreference(oldUserPreferenceId, Algorithm.TRI_RANK, request.getTenantId(),
			request.getUserId(), newUserPreferenceData));

		var clientRequest = ClientTriRankRecommendationRequest.builder()
			.tenantId(request.getTenantId())
			.uid(request.getUserId())
			.k(courseService.countByAlgorithm(Algorithm.TRI_RANK, request.getTenantId()))
			.preferences(toPolePreferences(request.getAttributeToScore()))
			.removeSeen(REMOVE_SEEN)
			.build();

		var response = triRankClient.getRecommendation(clientRequest);
		var filteredCourseCodes = courseService.getFilteredCourseCodes(Algorithm.TRI_RANK, request.getTenantId(),
			request.getUserId(), request.getFilterCoursesOptions(), request.getCustomFilteredCourseCodes());
		var recommendationCourseCodes = (response.recommendations() == null ? List.<String>of() :
			response.recommendations())
			.stream()
			.filter(courseCode -> !filteredCourseCodes.contains(courseCode))
			.distinct()
			.toList();
		var itemIdToItemAspects = getItemIdToItemAspects(request.getTenantId(), recommendationCourseCodes);

		var data = TriRankRecommendationResultData.builder()
			.courseCodes(recommendationCourseCodes)
			.filterCoursesOptions(request.getFilterCoursesOptions())
			.customFilteredCourseCodes(request.getCustomFilteredCourseCodes())
			.build();

		var savedRecommendationResult = recommendationResultRepository.save(
			new RecommendationResult(null, Algorithm.TRI_RANK, request.getTenantId(), request.getUserId(), data));

		return toServerTriRankRecommendationResult(savedRecommendationResult.getId(), data, itemIdToItemAspects,
			request.getUserId(), request.getTenantId());
	}

	@Transactional(readOnly = true)
	public ServerTriRankRecommendationResult getLatestTriRankRecommendationResult(String userId, Long tenantId) {
		var latestRecommendationResult = recommendationResultRepository.getLatestRecommendationResult(
			Algorithm.TRI_RANK,
			tenantId, userId);
		return latestRecommendationResult.map(recommendationResult -> {
			var recommendationResultData = (TriRankRecommendationResultData)recommendationResult.getData();
			var itemIdToItemAspects = getItemIdToItemAspects(tenantId, recommendationResultData.courseCodes());
			return toServerTriRankRecommendationResult(recommendationResult.getId(), recommendationResultData,
				itemIdToItemAspects, userId, tenantId);
		}).orElse(null);
	}

	/**
	 * Splits each bipolar target onto the two pole aspects TriRank can actually represent.
	 *
	 * <p>A target of 1 puts full positive weight on {@code <attribute>@low} and full negative weight on
	 * {@code <attribute>@high}, so a course at the rejected end is actively pushed down rather than
	 * merely not pulled up. A target of 3 - the neutral midpoint, and the form's default - weights
	 * neither pole, which is what "no leaning either way" should mean. An attribute the caller omitted,
	 * or sent as null, likewise contributes nothing; previously a null was coerced to 0.0 and ended up
	 * indistinguishable from an explicit request for the low pole.
	 *
	 * @see TriRankAspects
	 */
	List<List<Object>> toPolePreferences(Map<String, Double> attributeToScore) {
		if (attributeToScore == null) {
			return List.of();
		}

		List<List<Object>> preferences = new ArrayList<>();
		attributeToScore.forEach((attributeValue, target) -> {
			if (attributeValue == null || target == null) {
				return;
			}
			var lowWeight = TriRankAspects.lowPoleWeight(target);
			if (lowWeight != 0) {
				preferences.add(List.of(TriRankAspects.lowPoleAspect(attributeValue), lowWeight));
			}
			var highWeight = TriRankAspects.highPoleWeight(target);
			if (highWeight != 0) {
				preferences.add(List.of(TriRankAspects.highPoleAspect(attributeValue), highWeight));
			}
		});
		return preferences;
	}

	/**
	 * Builds the per-course explanation from the mean student rating of each attribute.
	 *
	 * <p>This replaces a min-max rescaling of the model's item-aspect matrix, which was wrong twice
	 * over. The quantity was wrong: because the export collapsed every kept rating to one constant
	 * token, that matrix held mention frequency, not the attribute's value - the displayed number
	 * correlated with mention rate at r=0.91. And the rescaling was wrong: normalising each course
	 * independently forces exactly one 5.0 per course and, whenever any aspect is unmentioned, exactly
	 * one 1.0, so 64% of all displayed scores landed at the extremes and only 21% in the middle. The
	 * mean rating puts 18% at the extremes and 53% in the middle, and is directly comparable to the
	 * target the user set.
	 */
	private Map<String, List<TriRankItemAspect>> getItemIdToItemAspects(Long tenantId, List<String> courseCodes) {
		var requestedCourseCodes = new LinkedHashSet<>(courseCodes);
		if (requestedCourseCodes.isEmpty()) {
			return Map.of();
		}

		var courseIdToCourseCode = courseRepository.findByAlgorithmAndTenantId(Algorithm.TRI_RANK, tenantId).stream()
			.filter(course -> Objects.nonNull(course.getCode()))
			.collect(Collectors.toMap(Course::getId, Course::getCode, (existing, ignored) -> existing));
		var attributeIdToValue = attributeRepository.findByAlgorithmAndTenantId(Algorithm.TRI_RANK, tenantId).stream()
			.filter(attribute -> Objects.nonNull(attribute.getValue()))
			.collect(Collectors.toMap(Attribute::getId, Attribute::getValue, (existing, ignored) -> existing));

		var aspectsByCourseCode = userCourseRatingRepository
			.findCourseAttributeScoreSummaries(Algorithm.TRI_RANK, tenantId).stream()
			.filter(summary -> courseIdToCourseCode.containsKey(summary.courseId()))
			.filter(summary -> attributeIdToValue.containsKey(summary.attributeId()))
			.collect(Collectors.groupingBy(summary -> courseIdToCourseCode.get(summary.courseId()),
				Collectors.mapping(summary -> TriRankItemAspect.builder()
					.aspect(attributeIdToValue.get(summary.attributeId()))
					.score(shrinkTowardsNeutral(summary.averageScore(), summary.ratingCount()))
					.build(), Collectors.toList())));

		Map<String, List<TriRankItemAspect>> itemIdToItemAspects = new LinkedHashMap<>();
		for (var courseCode : requestedCourseCodes) {
			itemIdToItemAspects.put(courseCode, aspectsByCourseCode.getOrDefault(courseCode, List.of()).stream()
				.sorted(Comparator.comparing(TriRankItemAspect::aspect))
				.toList());
		}
		return itemIdToItemAspects;
	}

	/**
	 * Shrinks a mean towards the neutral midpoint in proportion to how few ratings back it, so the
	 * displayed confidence matches the evidence.
	 */
	private double shrinkTowardsNeutral(Double averageScore, Long ratingCount) {
		if (averageScore == null || ratingCount == null || ratingCount <= 0) {
			return TriRankAspects.NEUTRAL_SCORE;
		}
		return (ratingCount * averageScore + EXPLANATION_PRIOR_RATING_COUNT * TriRankAspects.NEUTRAL_SCORE)
			/ (ratingCount + EXPLANATION_PRIOR_RATING_COUNT);
	}
}

package com.hcmus.course_recommendation.recommendation.fs.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.common.exception.NotFoundException;
import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.model.FSItemSentiment;
import com.hcmus.course_recommendation.course.model.FsCourseSentiment;
import com.hcmus.course_recommendation.course.model.UserCourseRating;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.course.repository.FsCourseSentimentRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseRatingRepository;
import com.hcmus.course_recommendation.course.service.CourseService;
import com.hcmus.course_recommendation.discuss.repository.PostCommentRepository;
import com.hcmus.course_recommendation.recommendation.RecommendationService;
import com.hcmus.course_recommendation.recommendation.fs.client.FSClient;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSExtractSentimentsRequest;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSItemReview;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSRefinedRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.fs.dto.FSRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.fs.dto.FSRefinedRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.fs.dto.ServerFSCategoryDetail;
import com.hcmus.course_recommendation.recommendation.fs.dto.ServerFSRecommendationResult;
import com.hcmus.course_recommendation.recommendation.fs.mapper.FSMapper;
import com.hcmus.course_recommendation.recommendation.fs.model.FsRecommendationResultData;
import com.hcmus.course_recommendation.recommendation.fs.model.RecommendationResult;
import com.hcmus.course_recommendation.recommendation.fs.model.UserPreference;
import com.hcmus.course_recommendation.recommendation.fs.model.UserPreferenceData;
import com.hcmus.course_recommendation.recommendation.fs.repository.RecommendationResultRepository;
import com.hcmus.course_recommendation.recommendation.fs.repository.UserPreferenceRepository;
import com.hcmus.course_recommendation.recommendation.model.Attribute;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FSService {
	private final FSClient fsClient;
	private final RecommendationResultRepository recommendationResultRepository;
	private final FSMapper fsMapper;
	private final CourseService courseService;
	private final UserPreferenceRepository fSUserPreferenceRepository;
	private final PostCommentRepository postCommentRepository;
	private final CourseRepository courseRepository;
	private final FsCourseSentimentRepository fsCourseSentimentRepository;
	private final RecommendationService recommendationService;
	private final UserCourseRatingRepository userCourseRatingRepository;

	private ServerFSRecommendationResult toServerFSRecommendationResult(Long id,
		FsRecommendationResultData recommendationResult, Map<String, List<FSItemSentiment>> itemIdToItemSentiments,
		String userId, Long tenantId) {
		var courseIds = Stream.concat(Stream.of(recommendationResult.topItemId()),
			recommendationResult.categoryDetails().stream().flatMap(x -> x.itemIds().stream())).toList();
		var courseIdToCourseDetail = courseService.getCourseIdToCourseDetailsByCourseIds(
			Algorithm.FS, tenantId, courseIds, userId);

		return ServerFSRecommendationResult.builder()
			.id(id)
			.topCourseDetail(courseIdToCourseDetail.get(recommendationResult.topItemId()))
			.categoryDetails(recommendationResult
				.categoryDetails()
				.stream()
				.map(fsCategoryDetail -> ServerFSCategoryDetail.builder()
					.category(fsCategoryDetail.category())
					.courseDetails(fsCategoryDetail.itemIds().stream().map(courseIdToCourseDetail::get).toList())
					.build())
				.toList())
			.filterCoursesOptions(recommendationResult.filterCoursesOptions())
			.customFilteredCourseCodes(recommendationResult.customFilteredCourseCodes())
			.itemIdToItemSentiments(itemIdToItemSentiments)
			.build();
	}

	@Transactional
	public ServerFSRecommendationResult getFSRecommendation(FSRecommendationRequest request) {
		var oldFSUserPreferenceId = fSUserPreferenceRepository.findByAlgorithmAndTenantIdAndUserId(
			Algorithm.FS, request.getTenantId(), request.getUserId()).map(UserPreference::getId).orElse(null);
		var newFSUserPreferenceData = UserPreferenceData.builder()
			.attributeToScore(request.getAttributeToPreferenceConfigure()
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().getTargetSentimentScore())))
			.build();

		fSUserPreferenceRepository.save(
			new UserPreference(oldFSUserPreferenceId, Algorithm.FS, request.getTenantId(), request.getUserId(),
				newFSUserPreferenceData));

		for (var preference : request.getAttributeToPreferenceConfigure().values()) {
			preference.setWeight(3.0);
		}

		var attributes = recommendationService.getAttributes(Algorithm.FS, request.getTenantId());
		var itemIdToItemSentiments = courseService.getFsItemIdToItemSentiments(
			request.getUserId(), request.getTenantId(), request.getFilterCoursesOptions(),
			request.getCustomFilteredCourseCodes());
		var response = fsClient.getRecommendation(ClientFSRecommendationRequest.builder()
			.attributes(attributes.stream().map(Attribute::getValue).toList())
			.itemIdToItemSentiments(fsMapper.toStringToClientFSItemSentiments(itemIdToItemSentiments))
			.attributeToPreferenceConfigure(
				fsMapper.toStringToClientFSPreferenceConfigure(request.getAttributeToPreferenceConfigure()))
			.build());

		var fsRecommendationResultData = fsMapper.toFeatureSentimentRecommendationResultData(response).toBuilder()
			.filterCoursesOptions(request.getFilterCoursesOptions())
			.customFilteredCourseCodes(request.getCustomFilteredCourseCodes())
			.build();

		var savedFSRecommendationResult = recommendationResultRepository.save(
			new RecommendationResult(null, Algorithm.FS, request.getTenantId(), request.getUserId(),
				fsRecommendationResultData));

		return toServerFSRecommendationResult(savedFSRecommendationResult.getId(),
			fsRecommendationResultData, itemIdToItemSentiments, request.getUserId(), request.getTenantId());
	}

	@Transactional
	public ServerFSRecommendationResult getFsRefinedRecommendation(FSRefinedRecommendationRequest request) {
		var attributes = recommendationService.getAttributes(Algorithm.FS, request.getTenantId());
		var recommendationResult = recommendationResultRepository.findById(request.getRecommendationId())
			.orElseThrow(NotFoundException::new);
		var recommendationResultData = (FsRecommendationResultData)recommendationResult.getData();
		var itemIdToItemSentiments = courseService.getFsItemIdToItemSentiments(
			request.getUserId(), request.getTenantId(), recommendationResultData.filterCoursesOptions(),
			recommendationResultData.customFilteredCourseCodes());
		var itemTradeoffVector = recommendationResultData.itemIdToTradeoffVector().get(request.getItemId());

		var clientRequest = ClientFSRefinedRecommendationRequest.builder()
			.attributes(attributes.stream().map(Attribute::getValue).toList())
			.itemIdToItemSentiments(fsMapper.toStringToClientFSItemSentiments(itemIdToItemSentiments))
			.oldAttributeToPreferenceConfigure(fsMapper.toStringToClientFSPreferenceConfigure(
				recommendationResultData.attributeToPreferenceConfigure()))
			.category(request.getCategory())
			.itemTradeoffVector(fsMapper.toClientFSTradeoffPairs(itemTradeoffVector))
			.build();

		var response = fsClient.getRefinedRecommendation(clientRequest);
		var data = fsMapper.toFeatureSentimentRecommendationResultData(response).toBuilder()
			.filterCoursesOptions(recommendationResultData.filterCoursesOptions())
			.customFilteredCourseCodes(recommendationResultData.customFilteredCourseCodes())
			.build();
		var savedFSRecommendationResult = recommendationResultRepository.save(
			new RecommendationResult(null, Algorithm.FS, request.getTenantId(), request.getUserId(), data));
		return toServerFSRecommendationResult(savedFSRecommendationResult.getId(), data, itemIdToItemSentiments,
			request.getUserId(), request.getTenantId());
	}

	@Transactional(readOnly = true)
	public ServerFSRecommendationResult getLatestFsRecommendationResult(String userId, Long tenantId) {
		var lastestFSRecommendationResult = recommendationResultRepository.getLatestFSRecommendationResult(
			Algorithm.FS,
			tenantId, userId);
		return lastestFSRecommendationResult.map(
				recommendationResult -> {
					var fsRecommendationResultData = (FsRecommendationResultData)recommendationResult.getData();
					var itemIdToItemSentiments = courseService.getFsItemIdToItemSentiments(
						userId, tenantId, fsRecommendationResultData.filterCoursesOptions(),
						fsRecommendationResultData.customFilteredCourseCodes());
					return toServerFSRecommendationResult(recommendationResult.getId(), fsRecommendationResultData,
						itemIdToItemSentiments, userId, tenantId);
				})
			.orElse(null);
	}

	@Transactional
	public void updateItemSentiments(Long tenantId) {
		var postComments = postCommentRepository.findAll();

		var clientFsItemReviews = postComments.stream().map(postComment -> ClientFSItemReview.builder()
				.userId(postComment.getUserId())
				.itemId(postComment.getCourseId())
				.reviewText(postComment.getContent())
				.build())
			// .limit(5)
			.toList();

		var clientFsExtractSentimentsRequest = ClientFSExtractSentimentsRequest.builder()
			.reviews(clientFsItemReviews)
			.attributes(
				recommendationService.getAttributes(Algorithm.FS, tenantId).stream().map(Attribute::getValue).toList())
			.build();

		var clientFsExtractSentimentsResult = fsClient.getSentiments(clientFsExtractSentimentsRequest);

		var savedCourses = courseRepository.saveAll(clientFsExtractSentimentsResult.stream()
			.map(sentiment -> Course.builder()
				.code(sentiment.itemId())
				.algorithm(Algorithm.FS)
				.tenantId(tenantId)
				.build())
			.toList());
		fsCourseSentimentRepository.saveAll(savedCourses.stream()
			.map(course -> {
				var sentiment = clientFsExtractSentimentsResult.stream()
					.filter(s -> s.itemId().equals(course.getCode()))
					.findFirst()
					.orElseThrow();
				return fsCourseSentimentRepository.findByCourseId(course.getId())
					.map(existing -> existing.toBuilder()
						.itemSentiments(fsMapper.toFSItemSentiments(sentiment.itemSentiments()))
						.build())
					.orElse(FsCourseSentiment.builder()
						.courseId(course.getId())
						.itemSentiments(fsMapper.toFSItemSentiments(sentiment.itemSentiments()))
						.build());
			})
			.toList());
	}

	@Transactional
	public void updateCoursesSentiments(Long tenantId) {
		fsCourseSentimentRepository.deleteByTenantId(tenantId);

		var attributes = recommendationService.getAttributes(Algorithm.FS, tenantId);
		var courses = courseRepository.findByAlgorithmAndTenantId(Algorithm.FS, tenantId);
		var userCourseRatings = userCourseRatingRepository.findByAlgorithmAndTenantId(Algorithm.FS, tenantId);

		var averageScoreByCourseAndAttribute = userCourseRatings.stream()
			.collect(Collectors.groupingBy(
				rating -> Map.entry(rating.getCourseId(), rating.getAttributeId()),
				Collectors.averagingInt(UserCourseRating::getScore)));

		// Records were just deleted above, so every sentiment here is newly built rather than updated.
		var newSentiments = courses.stream().map(course -> {
			var itemSentiments = attributes.stream().map(attribute -> {
				var averageScore = averageScoreByCourseAndAttribute.getOrDefault(
					Map.entry(course.getId(), attribute.getId()), 3.0);

				return FSItemSentiment.builder()
					.attribute(attribute.getValue())
					.sentimentScore(averageScore)
					.build();
			}).toList();

			return FsCourseSentiment.builder()
				.courseId(course.getId())
				.itemSentiments(itemSentiments)
				.build();
		}).toList();

		fsCourseSentimentRepository.saveAll(newSentiments);
	}
}

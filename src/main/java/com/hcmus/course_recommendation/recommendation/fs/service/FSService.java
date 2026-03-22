package com.hcmus.course_recommendation.recommendation.fs.service;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.common.exception.NotFoundException;
import com.hcmus.course_recommendation.course.dto.Domain;
import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.model.Dataset;
import com.hcmus.course_recommendation.course.model.FSItemSentiment;
import com.hcmus.course_recommendation.course.model.FsCourseExtraData;
import com.hcmus.course_recommendation.course.model.UserCourseRating;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
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
import com.hcmus.course_recommendation.recommendation.fs.model.RecommendationResult;
import com.hcmus.course_recommendation.recommendation.fs.model.UserPreference;
import com.hcmus.course_recommendation.recommendation.fs.model.UserPreferenceData;
import com.hcmus.course_recommendation.recommendation.fs.repository.RecommendationResultRepository;
import com.hcmus.course_recommendation.recommendation.fs.repository.UserPreferenceRepository;

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
	private final RecommendationService recommendationService;
	private final UserCourseRatingRepository userCourseRatingRepository;

	private ServerFSRecommendationResult toServerFSRecommendationResult(Dataset dataset,
		RecommendationResult recommendationResult,
		String userId) {
		var courseIds = Stream.concat(Stream.of(recommendationResult.getData().topItemId()),
			recommendationResult.getData().categoryDetails().stream().flatMap(x -> x.itemIds().stream())).toList();
		var courseIdToCourseDetail = courseService.getCourseIdToCourseDetailsByCourseIds(
			Domain.builder().algorithm(Algorithm.FS).dataset(dataset).build(), courseIds, userId);

		return ServerFSRecommendationResult.builder()
			.id(recommendationResult.getId())
			.attributeToPreferenceConfigure(recommendationResult.getData().attributeToPreferenceConfigure())
			.topCourseDetail(courseIdToCourseDetail.get(recommendationResult.getData().topItemId()))
			.categoryDetails(recommendationResult.getData()
				.categoryDetails()
				.stream()
				.map(fsCategoryDetail -> ServerFSCategoryDetail.builder()
					.category(fsCategoryDetail.category())
					.courseDetails(fsCategoryDetail.itemIds().stream().map(courseIdToCourseDetail::get).toList())
					.build())
				.toList())
			.filterCoursesOptions(recommendationResult.getData().filterCoursesOptions())
			.customFilteredCourseCodes(recommendationResult.getData().customFilteredCourseCodes())
			.build();
	}

	@Transactional
	public ServerFSRecommendationResult getFSRecommendation(FSRecommendationRequest request) {
		var oldFSUserPreferenceId = fSUserPreferenceRepository.findByDatasetAndAlgorithmAndUserId(request.getDataset(),
			Algorithm.FS,
			request.getUserId()).map(UserPreference::getId).orElse(null);
		var newFSUserPreferenceData = UserPreferenceData.builder()
			.attributeToScore(request.getAttributeToPreferenceConfigure()
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().getTargetSentimentScore())))
			.build();

		fSUserPreferenceRepository.save(
			new UserPreference(oldFSUserPreferenceId, request.getDataset(), Algorithm.FS, request.getUserId(),
				newFSUserPreferenceData));

		for (var preference : request.getAttributeToPreferenceConfigure().values()) {
			preference.setWeight(3.0);
		}

		var attributeValues = recommendationService.getAttributeValues(request.getDataset(), Algorithm.FS);
		var itemIdToItemSentiments = courseService.getFsItemIdToItemSentiments(request.getDataset(),
			request.getUserId(), request.getFilterCoursesOptions(), request.getCustomFilteredCourseCodes());
		var response = fsClient.getRecommendation(ClientFSRecommendationRequest.builder()
			.attributes(attributeValues)
			.itemIdToItemSentiments(fsMapper.toStringToClientFSItemSentiments(itemIdToItemSentiments))
			.attributeToPreferenceConfigure(
				fsMapper.toStringToClientFSPreferenceConfigure(request.getAttributeToPreferenceConfigure()))
			.build());

		var fsRecommendationResultData = fsMapper.toFeatureSentimentRecommendationResultData(response).toBuilder()
			.filterCoursesOptions(request.getFilterCoursesOptions())
			.customFilteredCourseCodes(request.getCustomFilteredCourseCodes())
			.build();

		var savedFSRecommendationResult = recommendationResultRepository.save(
			new RecommendationResult(null, request.getDataset(), Algorithm.FS, request.getUserId(),
				fsRecommendationResultData));

		return toServerFSRecommendationResult(request.getDataset(), savedFSRecommendationResult, request.getUserId());
	}

	@Transactional
	public ServerFSRecommendationResult getFsRefinedRecommendation(FSRefinedRecommendationRequest request) {
		var attributeValues = recommendationService.getAttributeValues(request.getDataset(), Algorithm.FS);
		var recommendationResult = recommendationResultRepository.findById(request.getRecommendationId())
			.orElseThrow(NotFoundException::new);
		var recommendationResultData = recommendationResult.getData();
		var itemIdToItemSentiments = courseService.getFsItemIdToItemSentiments(request.getDataset(),
			request.getUserId(), recommendationResultData.filterCoursesOptions(),
			recommendationResultData.customFilteredCourseCodes());
		var itemTradeoffVector = recommendationResultData.itemIdToTradeoffVector().get(request.getItemId());

		var clientRequest = ClientFSRefinedRecommendationRequest.builder()
			.attributes(attributeValues)
			.itemIdToItemSentiments(fsMapper.toStringToClientFSItemSentiments(itemIdToItemSentiments))
			.oldAttributeToPreferenceConfigure(fsMapper.toStringToClientFSPreferenceConfigure(
				recommendationResultData.attributeToPreferenceConfigure()))
			.category(request.getCategory())
			.itemTradeoffVector(fsMapper.toClientFSTradeoffPairs(itemTradeoffVector))
			.build();

		var response = fsClient.getRefinedRecommendation(clientRequest);
		var data = fsMapper.toFeatureSentimentRecommendationResultData(response);
		var savedFSRecommendationResult = recommendationResultRepository.save(
			new RecommendationResult(null, request.getDataset(), Algorithm.FS, request.getUserId(), data));
		return toServerFSRecommendationResult(request.getDataset(), savedFSRecommendationResult, request.getUserId());
	}

	@Transactional(readOnly = true)
	public ServerFSRecommendationResult getLatestFsRecommendationResult(Dataset dataset, String userId) {
		var lastestFSRecommendationResult = recommendationResultRepository.getLatestFSRecommendationResult(dataset,
			Algorithm.FS,
			userId);
		return lastestFSRecommendationResult.map(x -> toServerFSRecommendationResult(dataset, x, userId)).orElse(null);
	}

	@Transactional
	public void updateItemSentiments() {
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
			.attributes(recommendationService.getAttributeValues(Dataset.FIT, Algorithm.FS))
			.build();

		var clientFsExtractSentimentsResult = fsClient.getSentiments(clientFsExtractSentimentsRequest);

		courseRepository.saveAll(clientFsExtractSentimentsResult.stream().map(sentiment -> Course.builder()
			.code(sentiment.itemId())
			.dataset(Dataset.FIT)
			.algorithm(Algorithm.FS)
			.extraData(FsCourseExtraData.builder()
				.itemSentiments(fsMapper.toFSItemSentiments(sentiment.itemSentiments()))
				.build())
			.build()).toList());
	}

	@Transactional
	public void updateCoursesSentiments(Dataset dataset) {
		var attributeValues = recommendationService.getAttributeValues(dataset, Algorithm.FS);

		var courses = courseRepository.findByAlgorithmAndDataset(Algorithm.FS, dataset);

		var userCourseRatings = userCourseRatingRepository.findByAlgorithmAndDataset(Algorithm.FS, dataset);

		var newCourses = courses.stream().map(course -> {
			var ratingsOfCourse = userCourseRatings.stream()
				.filter(rating -> rating.getCourseId().equals(course.getId()))
				.toList();

			var itemSentiments = attributeValues.stream().map(attributeValue -> {
				var ratingsOfAttribute = ratingsOfCourse.stream()
					.filter(rating -> rating.getAttributeValue().equals(attributeValue))
					.toList();

				var averageScore =
					ratingsOfAttribute.stream().mapToInt(UserCourseRating::getScore).average().orElse(3.0);

				return FSItemSentiment.builder()
					.attribute(attributeValue)
					.sentimentScore(averageScore)
					.build();
			}).toList();

			return course.toBuilder()
				.extraData(FsCourseExtraData.builder()
					.itemSentiments(itemSentiments)
					.build())
				.build();
		}).toList();

		courseRepository.saveAll(newCourses);
	}
}

package com.hcmus.course_recommendation.recommendation.fs.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.common.exception.NotFoundException;
import com.hcmus.course_recommendation.course.model.CourseDataset;
import com.hcmus.course_recommendation.course.service.CourseService;
import com.hcmus.course_recommendation.recommendation.fs.client.FSClient;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.fs.client.dto.ClientFSRefinedRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.fs.dto.FSRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.fs.dto.FSRefinedRecommendationRequest;
import com.hcmus.course_recommendation.recommendation.fs.dto.ServerFSCategoryDetail;
import com.hcmus.course_recommendation.recommendation.fs.dto.ServerFSRecommendationResult;
import com.hcmus.course_recommendation.recommendation.fs.mapper.FSMapper;
import com.hcmus.course_recommendation.recommendation.fs.model.FSAttribute;
import com.hcmus.course_recommendation.recommendation.fs.model.FSRecommendationResult;
import com.hcmus.course_recommendation.recommendation.fs.model.FSUserPreference;
import com.hcmus.course_recommendation.recommendation.fs.model.FSUserPreferenceData;
import com.hcmus.course_recommendation.recommendation.fs.repository.FSRecommendationResultRepository;
import com.hcmus.course_recommendation.recommendation.fs.repository.FSUserPreferenceRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FSService {
	private final FSClient fsClient;
	private final FSRecommendationResultRepository fsRecommendationResultRepository;
	private final FSMapper fsMapper;
	private final CourseService courseService;
	private final FSUserPreferenceRepository fSUserPreferenceRepository;

	private ServerFSRecommendationResult toServerFSRecommendationResult(FSRecommendationResult fsRecommendationResult,
		String userId) {
		var courseIds = Stream.concat(Stream.of(fsRecommendationResult.getData().topItemId()),
			fsRecommendationResult.getData().categoryDetails().stream().flatMap(x -> x.itemIds().stream())).toList();
		var courseIdToCourseDetail = courseService.getCourseIdToCourseDetailsByCourseIds(courseIds, userId);

		return ServerFSRecommendationResult.builder()
			.id(fsRecommendationResult.getId())
			.attributeToPreferenceConfigure(fsRecommendationResult.getData().attributeToPreferenceConfigure())
			.topCourseDetail(courseIdToCourseDetail.get(fsRecommendationResult.getData().topItemId()))
			.categoryDetails(fsRecommendationResult.getData()
				.categoryDetails()
				.stream()
				.map(fsCategoryDetail -> ServerFSCategoryDetail.builder()
					.category(fsCategoryDetail.category())
					.courseDetails(fsCategoryDetail.itemIds().stream().map(courseIdToCourseDetail::get).toList())
					.build())
				.toList())
			.build();
	}

	@Transactional
	public ServerFSRecommendationResult getFSRecommendation(FSRecommendationRequest request) {
		var oldFSUserPreferenceId = fSUserPreferenceRepository.findByDatasetAndUserId(request.getDataset(),
			request.getUserId()).map(FSUserPreference::getId).orElse(null);
		var newFSUserPreferenceData = FSUserPreferenceData.builder()
			.attributeToTargetSentimentScore(request.getAttributeToPreferenceConfigure()
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().getTargetSentimentScore())))
			.build();

		fSUserPreferenceRepository.save(
			new FSUserPreference(oldFSUserPreferenceId, request.getDataset(), request.getUserId(),
				newFSUserPreferenceData));

		for (var preference : request.getAttributeToPreferenceConfigure().values()) {
			preference.setWeight(3.0);
		}
		
		var attributeValues = getAttributeValues(request.getDataset());
		var itemIdToItemSentiments = courseService.getFSItemIdToItemSentiments(request.getDataset());
		var response = fsClient.getRecommendation(ClientFSRecommendationRequest.builder()
			.attributes(attributeValues)
			.itemIdToItemSentiments(fsMapper.toStringToClientFSItemSentiments(itemIdToItemSentiments))
			.attributeToPreferenceConfigure(
				fsMapper.toStringToClientFSPreferenceConfigure(request.getAttributeToPreferenceConfigure()))
			.build());

		var fsRecommendationResultData = fsMapper.toFeatureSentimentRecommendationResultData(response);

		var savedFSRecommendationResult = fsRecommendationResultRepository.save(
			new FSRecommendationResult(null, request.getDataset(), request.getUserId(), fsRecommendationResultData));

		return toServerFSRecommendationResult(savedFSRecommendationResult, request.getUserId());
	}

	@Transactional
	public ServerFSRecommendationResult getFSRefinedRecommendation(FSRefinedRecommendationRequest request) {
		var attributeValues = getAttributeValues(request.getDataset());
		var itemIdToItemSentiments = courseService.getFSItemIdToItemSentiments(request.getDataset());
		var recommendationResult = fsRecommendationResultRepository.findById(request.getRecommendationId())
			.orElseThrow(NotFoundException::new);
		var recommendationResultData = recommendationResult.getData();
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
		var savedFSRecommendationResult = fsRecommendationResultRepository.save(
			new FSRecommendationResult(null, request.getDataset(), request.getUserId(), data));
		return toServerFSRecommendationResult(savedFSRecommendationResult, request.getUserId());
	}

	public List<FSAttribute> getAttributes(CourseDataset dataset) {
		if (CourseDataset.FIT.equals(dataset)) {
			return List.of(new FSAttribute("content", "Nội dung"), new FSAttribute("instructor", "Giảng viên"),
				new FSAttribute("workload", "Khối lượng bài tập"), new FSAttribute("difficulty", "Độ khó"),
				new FSAttribute("practicality", "Tính thực tiễn"), new FSAttribute("materials", "Tài liệu"));
		}

		if (CourseDataset.CELLPHONE.equals(dataset)) {
			return List.of(new FSAttribute("screen", "Màn hình"), new FSAttribute("case", "Thiết kế"),
				new FSAttribute("battery", "Pin"), new FSAttribute("money", "Giá cả"),
				new FSAttribute("sound", "Âm thanh"), new FSAttribute("charging", "Sạc"));
		}

		return List.of();
	}

	public Map<String, String> getAttributeValueToLabel(CourseDataset dataset) {
		return getAttributes(dataset).stream().collect(Collectors.toMap(FSAttribute::value, FSAttribute::label));
	}

	public List<String> getAttributeValues(CourseDataset dataset) {
		return getAttributes(dataset).stream().map(FSAttribute::value).toList();
	}

	@Transactional(readOnly = true)
	public Map<String, Double> getAttributeToTargetSentimentScore(CourseDataset dataset, String userId) {
		return fSUserPreferenceRepository.findByDatasetAndUserId(dataset, userId)
			.map(x -> x.getData().attributeToTargetSentimentScore())
			.orElse(null);
	}

	@Transactional(readOnly = true)
	public ServerFSRecommendationResult getLatestRecommendationResult(CourseDataset dataset, String userId) {
		var lastestFSRecommendationResult = fsRecommendationResultRepository.getLatestFSRecommendationResult(dataset,
			userId);
		return lastestFSRecommendationResult.map(x -> toServerFSRecommendationResult(x, userId)).orElse(null);
	}
}

package com.hcmus.course_recommendation.course.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.dto.AddUserCourseRequest;
import com.hcmus.course_recommendation.course.dto.CourseDetail;
import com.hcmus.course_recommendation.course.dto.Domain;
import com.hcmus.course_recommendation.course.dto.GetCourseDetailRequest;
import com.hcmus.course_recommendation.course.dto.GetCourseDetailsRequest;
import com.hcmus.course_recommendation.course.dto.GetCoursesOfUserRequest;
import com.hcmus.course_recommendation.course.dto.GetCoursesRequest;
import com.hcmus.course_recommendation.course.dto.UpdateUserCoursesRequest;
import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.model.Dataset;
import com.hcmus.course_recommendation.course.model.FSItemSentiment;
import com.hcmus.course_recommendation.course.model.FsCourseExtraData;
import com.hcmus.course_recommendation.course.model.UserCourseRating;
import com.hcmus.course_recommendation.course.model.UserCourseStatus;
import com.hcmus.course_recommendation.course.model.UserCourseStatusEnum;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseRatingRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseStatusRepository;
import com.hcmus.course_recommendation.recommendation.model.FilterCoursesOption;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CourseService {
	private final UserCourseStatusRepository userCourseStatusRepository;
	private final CourseRepository courseRepository;
	private final UserCourseRatingRepository userCourseRatingRepository;

	@Transactional
	public void updateUserCourses(UpdateUserCoursesRequest request) {
		var deletingUserCourseIds = userCourseStatusRepository.findByUserIdAndStatusAndDomain(request.getUserId(),
			request.getUserCourseStatus(), request.getDomain().getAlgorithm(),
			request.getDomain().getDataset()).stream().map(UserCourseStatus::getId).toList();

		userCourseStatusRepository.deleteAllById(deletingUserCourseIds);

		userCourseStatusRepository.saveAll(request.getCourseIds()
			.stream()
			.map(courseId -> UserCourseStatus.builder()
				.status(request.getUserCourseStatus())
				.userId(request.getUserId())
				.courseId(courseId)
				.build())
			.toList());
	}

	@Transactional
	public void addUserCourse(AddUserCourseRequest request) {
		userCourseStatusRepository.save(UserCourseStatus.builder()
			.userId(request.getUserId())
			.courseId(request.getCourseId())
			.status(request.getStatus())
			.build());
	}

	@Transactional
	public void deleteUserCourse(String userId, String courseId) {
		userCourseStatusRepository.deleteByUserIdAndCourseId(userId, courseId);
	}

	@Transactional(readOnly = true)
	public List<CourseDetail> getCoursesOfUser(GetCoursesOfUserRequest request) {
		var userCourses = userCourseStatusRepository.findByUserIdAndStatusAndDomain(request.getUserId(),
			request.getUserCourseStatus(), request.getDomain().getAlgorithm(),
			request.getDomain().getDataset());
		var courseCodes = courseRepository.findByIdIn(userCourses.stream().map(UserCourseStatus::getCourseId).toList())
			.stream()
			.map(Course::getCode)
			.toList();

		return toCourseDetails(request.getDomain(), courseCodes, request.getUserId());
	}

	@Transactional(readOnly = true)
	public Map<String, CourseDetail> getCourseIdToCourseDetailsByCourseIds(Domain domain, List<String> courseIds,
		String userId) {
		return toCourseDetails(domain, courseIds, userId).stream()
			.collect(Collectors.toMap(courseDetail -> courseDetail.course().getCode(), Function.identity()));
	}

	@Transactional(readOnly = true)
	public List<CourseDetail> toCourseDetails(Domain domain, List<String> courseCodes, String userId) {
		var courses = courseRepository.findByAlgorithmAndDatasetAndCodeIn(domain.getAlgorithm(),
			domain.getDataset(), courseCodes);

		var userCourseStatuses = userCourseStatusRepository.findByUserId(userId);
		var courseIdToUserCourseStatuses = userCourseStatuses.stream()
			.collect(Collectors.groupingBy(UserCourseStatus::getCourseId));

		var userCourseRatings = userCourseRatingRepository.findByUserId(userId);
		var courseIdToUserCourseRatings = userCourseRatings.stream()
			.collect(Collectors.groupingBy(UserCourseRating::getCourseId));

		return courses.stream()
			.map(course -> CourseDetail.builder()
				.course(course)
				.userCourseStatuses(courseIdToUserCourseStatuses.getOrDefault(course.getId(), List.of())
					.stream()
					.map(UserCourseStatus::getStatus)
					.toList())
				.userAttributeValueToRatingScore(courseIdToUserCourseRatings.getOrDefault(course.getId(), List.of())
					.stream()
					.collect(Collectors.toMap(UserCourseRating::getAttributeValue, UserCourseRating::getScore)))
				.build())
			.toList();
	}

	@Transactional(readOnly = true)
	public List<CourseDetail> getCourseDetails(GetCourseDetailsRequest request) {
		return toCourseDetails(request.getDomain(),
			courseRepository.findByAlgorithmAndDataset(request.getDomain().getAlgorithm(), request.getDomain()
				.getDataset()).stream().map(Course::getCode).toList(),
			request.getUserId());
	}

	@Transactional(readOnly = true)
	public CourseDetail getCourseDetail(GetCourseDetailRequest request) {
		var courseDetails = toCourseDetails(request.getDomain(), List.of(request.getCourseCode()), request.getUserId());
		try {
			return courseDetails.getFirst();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Transactional(readOnly = true)
	public List<Course> getCourses(GetCoursesRequest request) {
		return courseRepository.findByAlgorithmAndDataset(request.getDomain().getAlgorithm(),
			request.getDomain().getDataset());
	}

	@Transactional(readOnly = true)
	public List<Course> findCoursesByIds(Domain domain, List<String> courseIds) {
		return courseRepository.findByAlgorithmAndDatasetAndCodeIn(domain.getAlgorithm(), domain.getDataset(),
			courseIds);
	}

	@Transactional(readOnly = true)
	public Map<String, Course> getCourseIdToCourseMapByCourseIds(Domain domain, List<String> courseIds) {
		return findCoursesByIds(domain, courseIds).stream()
			.collect(Collectors.toMap(Course::getCode, Function.identity()));
	}

	@Transactional(readOnly = true)
	public Map<String, List<FSItemSentiment>> getFsItemIdToItemSentiments(Dataset dataset, String userId,
		List<FilterCoursesOption> filterCoursesOptions, List<String> customFilteredCourseCodes) {
		var planningCourseIds = userCourseStatusRepository.findByUserIdAndStatusAndDomain(userId,
			UserCourseStatusEnum.PLANNING, Algorithm.FS, dataset).stream().map(UserCourseStatus::getCourseId).toList();
		var completedCourseIds = userCourseStatusRepository.findByUserIdAndStatusAndDomain(userId,
			UserCourseStatusEnum.COMPLETED, Algorithm.FS, dataset).stream().map(UserCourseStatus::getCourseId).toList();
		var customFilteredCourseIds = courseRepository.findByAlgorithmAndDatasetAndCodeIn(Algorithm.FS, dataset,
			customFilteredCourseCodes).stream().map(Course::getId).toList();

		var finalFilteredCourseIds = new ArrayList<Long>();
		if (filterCoursesOptions.contains(FilterCoursesOption.PLANNING)) {
			finalFilteredCourseIds.addAll(planningCourseIds);
		}
		if (filterCoursesOptions.contains(FilterCoursesOption.COMPLETED)) {
			finalFilteredCourseIds.addAll(completedCourseIds);
		}
		if (filterCoursesOptions.contains(FilterCoursesOption.CUSTOM)) {
			finalFilteredCourseIds.addAll(customFilteredCourseIds);
		}

		return courseRepository.findByAlgorithmAndDataset(Algorithm.FS, dataset)
			.stream()
			.filter(course -> !finalFilteredCourseIds.contains(course.getId()))
			.collect(
				Collectors.toMap(Course::getCode,
					course -> ((FsCourseExtraData)course.getExtraData()).itemSentiments()));
	}

	@Transactional
	public void rateCourse(String userId, Long courseId, String attributeValue, Integer score) {
		var oldUserCourseRating = userCourseRatingRepository.findByUserIdAndCourseIdAndAttributeValue(userId, courseId,
			attributeValue);

		if (oldUserCourseRating.isEmpty()) {
			userCourseRatingRepository.save(UserCourseRating.builder()
				.courseId(courseId)
				.userId(userId)
				.attributeValue(attributeValue)
				.score(score)
				.build());
		} else {
			userCourseRatingRepository.save(oldUserCourseRating.get().toBuilder()
				.score(score)
				.build());
		}
	}
}

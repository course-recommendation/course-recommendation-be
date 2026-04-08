package com.hcmus.course_recommendation.course.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.dto.CourseDetail;
import com.hcmus.course_recommendation.course.dto.GetCourseDetailRequest;
import com.hcmus.course_recommendation.course.dto.GetCourseDetailsRequest;
import com.hcmus.course_recommendation.course.dto.GetCoursesOfUserRequest;
import com.hcmus.course_recommendation.course.dto.GetCoursesRequest;
import com.hcmus.course_recommendation.course.dto.UpdateUserCourseStatusRequest;
import com.hcmus.course_recommendation.course.dto.UpdateUserCourseStatusesRequest;
import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Course;
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
	public void updateUserCourseStatuses(UpdateUserCourseStatusesRequest request) {
		var userCourseStatusesWithIdsAndNotRequestStatus = userCourseStatusRepository.findByUserIdAndAlgorithmAndNotStatusAndCourseIdIn(
			request.getUserId(), request.getAlgorithm(), request.getUserCourseStatus(), request.getCourseIds());
		var userCourseStatusesWithRequestStatus = userCourseStatusRepository.findByUserIdAndStatusAndAlgorithm(
			request.getUserId(),
			request.getUserCourseStatus(), request.getAlgorithm());
		var deletingUserCourseIds = Stream.concat(userCourseStatusesWithIdsAndNotRequestStatus.stream(),
			userCourseStatusesWithRequestStatus.stream()).map(UserCourseStatus::getId).toList();

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
	public void updateUserCourseStatus(UpdateUserCourseStatusRequest request) {
		var oldUserCourseStatusOpt = userCourseStatusRepository.findByUserIdAndCourseIdAndStatus(request.getUserId(),
			request.getCourseId(), request.getStatus());

		if (oldUserCourseStatusOpt.isPresent()) {
			userCourseStatusRepository.deleteById(oldUserCourseStatusOpt.get().getId());
			return;
		}

		userCourseStatusRepository.deleteByUserIdAndCourseIdAndStatusNot(request.getUserId(), request.getCourseId(),
			request.getStatus());

		userCourseStatusRepository.save(UserCourseStatus.builder()
			.userId(request.getUserId())
			.courseId(request.getCourseId())
			.status(request.getStatus())
			.build());
	}

	@Transactional(readOnly = true)
	public List<CourseDetail> getCoursesOfUser(GetCoursesOfUserRequest request) {
		var userCourses = userCourseStatusRepository.findByUserIdAndStatusAndAlgorithm(request.getUserId(),
			request.getUserCourseStatus(), request.getAlgorithm());
		var courseCodes = courseRepository.findByIdIn(userCourses.stream().map(UserCourseStatus::getCourseId).toList())
			.stream()
			.map(Course::getCode)
			.toList();

		return toCourseDetails(request.getAlgorithm(), courseCodes, request.getUserId());
	}

	@Transactional(readOnly = true)
	public Map<String, CourseDetail> getCourseIdToCourseDetailsByCourseIds(Algorithm algorithm, List<String> courseIds,
		String userId) {
		return toCourseDetails(algorithm, courseIds, userId).stream()
			.collect(Collectors.toMap(courseDetail -> courseDetail.course().getCode(), Function.identity()));
	}

	@Transactional(readOnly = true)
	public List<CourseDetail> toCourseDetails(Algorithm algorithm, List<String> courseCodes, String userId) {
		var courses = courseRepository.findByAlgorithmAndCodeIn(algorithm, courseCodes);

		var userCourseStatuses = userCourseStatusRepository.findByUserId(userId);
		var courseIdToUserCourseStatus = userCourseStatuses.stream()
			.collect(Collectors.toMap(UserCourseStatus::getCourseId, Function.identity()));

		var userCourseRatings = userCourseRatingRepository.findByUserId(userId);
		var courseIdToUserCourseRatings = userCourseRatings.stream()
			.collect(Collectors.groupingBy(UserCourseRating::getCourseId));

		return courses.stream()
			.map(course -> CourseDetail.builder()
				.course(course)
				.userCourseStatus(Optional.ofNullable(courseIdToUserCourseStatus.get(course.getId()))
					.map(UserCourseStatus::getStatus)
					.orElse(null))
				.userAttributeValueToRatingScore(courseIdToUserCourseRatings.getOrDefault(course.getId(), List.of())
					.stream()
					.collect(Collectors.toMap(UserCourseRating::getAttributeValue, UserCourseRating::getScore)))
				.build())
			.toList();
	}

	@Transactional(readOnly = true)
	public List<CourseDetail> getCourseDetails(GetCourseDetailsRequest request) {
		if (request.getName() == null) {
			request.setName("");
		}
		return toCourseDetails(request.getAlgorithm(),
			courseRepository.findByAlgorithmAndNameLike(request.getAlgorithm(),
				String.format("%%%s%%", request.getName())).stream().map(Course::getCode).toList(),
			request.getUserId());
	}

	@Transactional(readOnly = true)
	public CourseDetail getCourseDetail(GetCourseDetailRequest request) {
		var courseDetails = toCourseDetails(request.getAlgorithm(), List.of(request.getCourseCode()),
			request.getUserId());
		try {
			return courseDetails.getFirst();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Transactional(readOnly = true)
	public List<Course> getCourses(GetCoursesRequest request) {
		return courseRepository.findByAlgorithm(request.getAlgorithm());
	}

	@Transactional(readOnly = true)
	public List<Course> findCoursesByIds(Algorithm algorithm, List<String> courseIds) {
		return courseRepository.findByAlgorithmAndCodeIn(algorithm,
			courseIds);
	}

	@Transactional(readOnly = true)
	public Map<String, Course> getCourseIdToCourseMapByCourseIds(Algorithm algorithm, List<String> courseIds) {
		return findCoursesByIds(algorithm, courseIds).stream()
			.collect(Collectors.toMap(Course::getCode, Function.identity()));
	}

	@Transactional(readOnly = true)
	public Map<String, List<FSItemSentiment>> getFsItemIdToItemSentiments(String userId,
		List<FilterCoursesOption> filterCoursesOptions, List<String> customFilteredCourseCodes) {
		var planningCourseIds = userCourseStatusRepository.findByUserIdAndStatusAndAlgorithm(userId,
			UserCourseStatusEnum.PLANNED, Algorithm.FS).stream().map(UserCourseStatus::getCourseId).toList();
		var completedCourseIds = userCourseStatusRepository.findByUserIdAndStatusAndAlgorithm(userId,
			UserCourseStatusEnum.COMPLETED, Algorithm.FS).stream().map(UserCourseStatus::getCourseId).toList();
		var customFilteredCourseIds = courseRepository.findByAlgorithmAndCodeIn(Algorithm.FS,
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

		return courseRepository.findByAlgorithm(Algorithm.FS)
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

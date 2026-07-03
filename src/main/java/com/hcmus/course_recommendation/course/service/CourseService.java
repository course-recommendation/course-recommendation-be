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
import com.hcmus.course_recommendation.course.model.FsCourseSentiment;
import com.hcmus.course_recommendation.course.model.UserCourseRating;
import com.hcmus.course_recommendation.course.model.UserCourseStatus;
import com.hcmus.course_recommendation.course.model.UserCourseStatusEnum;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.course.repository.FsCourseSentimentRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseRatingRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseStatusRepository;
import com.hcmus.course_recommendation.common.exception.GlobalErrorCode;
import com.hcmus.course_recommendation.common.exception.NotFoundException;
import com.hcmus.course_recommendation.recommendation.model.FilterCoursesOption;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CourseService {
	private final UserCourseStatusRepository userCourseStatusRepository;
	private final CourseRepository courseRepository;
	private final UserCourseRatingRepository userCourseRatingRepository;
	private final FsCourseSentimentRepository fsCourseSentimentRepository;

	@Transactional
	public void updateUserCourseStatuses(UpdateUserCourseStatusesRequest request) {
		var userCourseStatusesWithIdsAndNotRequestStatus = userCourseStatusRepository.findByUserIdAndAlgorithmAndTenantIdAndNotStatusAndCourseIdIn(
			request.getUserId(), request.getAlgorithm(), request.getTenantId(), request.getUserCourseStatus(),
			request.getCourseIds());
		var userCourseStatusesWithRequestStatus = userCourseStatusRepository.findByUserIdAndStatusAndAlgorithmAndTenantId(
			request.getUserId(), request.getUserCourseStatus(), request.getAlgorithm(), request.getTenantId());
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
		if (!courseRepository.existsByIdAndTenantId(request.getCourseId(), request.getTenantId())) {
			throw new NotFoundException(GlobalErrorCode.NOT_FOUND, request.getCourseId());
		}

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
		var userCourses = userCourseStatusRepository.findByUserIdAndStatusAndAlgorithmAndTenantId(request.getUserId(),
			request.getUserCourseStatus(), request.getAlgorithm(), request.getTenantId());
		var courseCodes = courseRepository.findByIdIn(userCourses.stream().map(UserCourseStatus::getCourseId).toList())
			.stream()
			.map(Course::getCode)
			.toList();

		return toCourseDetails(request.getAlgorithm(), request.getTenantId(), courseCodes, request.getUserId());
	}

	@Transactional(readOnly = true)
	public Map<String, CourseDetail> getCourseIdToCourseDetailsByCourseIds(Algorithm algorithm, Long tenantId,
		List<String> courseIds, String userId) {
		return toCourseDetails(algorithm, tenantId, courseIds, userId).stream()
			.collect(Collectors.toMap(courseDetail -> courseDetail.course().getCode(), Function.identity()));
	}

	@Transactional(readOnly = true)
	public List<CourseDetail> toCourseDetails(Algorithm algorithm, Long tenantId, List<String> courseCodes,
		String userId) {
		var courses = courseRepository.findByAlgorithmAndTenantIdAndCodeIn(algorithm, tenantId, courseCodes);

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
				.userAttributeIdToRatingScore(courseIdToUserCourseRatings.getOrDefault(course.getId(), List.of())
					.stream()
					.collect(Collectors.toMap(UserCourseRating::getAttributeId, UserCourseRating::getScore)))
				.build())
			.toList();
	}

	@Transactional(readOnly = true)
	public List<CourseDetail> getCourseDetails(GetCourseDetailsRequest request) {
		if (request.getName() == null) {
			request.setName("");
		}
		return toCourseDetails(request.getAlgorithm(), request.getTenantId(),
			courseRepository.findByAlgorithmAndTenantIdAndNameLike(request.getAlgorithm(), request.getTenantId(),
				String.format("%%%s%%", request.getName())).stream().map(Course::getCode).toList(),
			request.getUserId());
	}

	@Transactional(readOnly = true)
	public CourseDetail getCourseDetail(GetCourseDetailRequest request) {
		var courseDetails = toCourseDetails(request.getAlgorithm(), request.getTenantId(),
			List.of(request.getCourseCode()),
			request.getUserId());
		try {
			return courseDetails.getFirst();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Transactional(readOnly = true)
	public List<Course> getCourses(GetCoursesRequest request) {
		return courseRepository.findByAlgorithmAndTenantId(request.getAlgorithm(), request.getTenantId());
	}

	@Transactional(readOnly = true)
	public List<Course> findCoursesByIds(Algorithm algorithm, Long tenantId, List<String> courseIds) {
		return courseRepository.findByAlgorithmAndTenantIdAndCodeIn(algorithm, tenantId, courseIds);
	}

	@Transactional(readOnly = true)
	public Map<String, Course> getCourseIdToCourseMapByCourseIds(Algorithm algorithm, Long tenantId,
		List<String> courseIds) {
		return findCoursesByIds(algorithm, tenantId, courseIds).stream()
			.collect(Collectors.toMap(Course::getCode, Function.identity()));
	}

	@Transactional(readOnly = true)
	public List<String> getFilteredCourseCodes(Algorithm algorithm, Long tenantId, String userId,
		List<FilterCoursesOption> filterCoursesOptions, List<String> customFilteredCourseCodes) {
		var normalizedFilterCoursesOptions = Optional.ofNullable(filterCoursesOptions).orElse(List.of());
		var normalizedCustomFilteredCourseCodes = Optional.ofNullable(customFilteredCourseCodes).orElse(List.of());

		var planningCourseIds = userCourseStatusRepository.findByUserIdAndStatusAndAlgorithmAndTenantId(userId,
			UserCourseStatusEnum.PLANNED, algorithm, tenantId).stream().map(UserCourseStatus::getCourseId).toList();
		var completedCourseIds = userCourseStatusRepository.findByUserIdAndStatusAndAlgorithmAndTenantId(userId,
			UserCourseStatusEnum.COMPLETED, algorithm, tenantId).stream().map(UserCourseStatus::getCourseId).toList();
		var customFilteredCourseIds = courseRepository.findByAlgorithmAndTenantIdAndCodeIn(algorithm, tenantId,
			normalizedCustomFilteredCourseCodes).stream().map(Course::getId).toList();

		var finalFilteredCourseIds = new ArrayList<Long>();
		if (normalizedFilterCoursesOptions.contains(FilterCoursesOption.PLANNING)) {
			finalFilteredCourseIds.addAll(planningCourseIds);
		}
		if (normalizedFilterCoursesOptions.contains(FilterCoursesOption.COMPLETED)) {
			finalFilteredCourseIds.addAll(completedCourseIds);
		}
		finalFilteredCourseIds.addAll(customFilteredCourseIds);

		return courseRepository.findByIdIn(finalFilteredCourseIds).stream().map(Course::getCode).toList();
	}

	@Transactional(readOnly = true)
	public Map<String, List<FSItemSentiment>> getFsItemIdToItemSentiments(String userId,
		Long tenantId, List<FilterCoursesOption> filterCoursesOptions, List<String> customFilteredCourseCodes) {
		var filteredCourseCodes = getFilteredCourseCodes(Algorithm.FS, tenantId, userId, filterCoursesOptions,
			customFilteredCourseCodes);
		var courses = courseRepository.findByAlgorithmAndTenantId(Algorithm.FS, tenantId)
			.stream()
			.filter(course -> !filteredCourseCodes.contains(course.getCode()))
			.toList();
		var courseIdToCode = courses.stream().collect(Collectors.toMap(Course::getId, Course::getCode));
		var sentimentsByCourseId = fsCourseSentimentRepository
			.findByCourseIdIn(courses.stream().map(Course::getId).toList())
			.stream()
			.collect(Collectors.toMap(FsCourseSentiment::getCourseId, FsCourseSentiment::getItemSentiments));
		return courseIdToCode.entrySet().stream()
			.filter(entry -> sentimentsByCourseId.containsKey(entry.getKey()))
			.collect(Collectors.toMap(Map.Entry::getValue, entry -> sentimentsByCourseId.get(entry.getKey())));
	}

	@Transactional
	public void rateCourse(String userId, Long tenantId, Long courseId, Long attributeId, Integer score) {
		if (!courseRepository.existsByIdAndTenantId(courseId, tenantId)) {
			throw new NotFoundException(GlobalErrorCode.NOT_FOUND, courseId);
		}

		var oldUserCourseRating = userCourseRatingRepository.findByUserIdAndCourseIdAndAttributeId(userId, courseId,
			attributeId);

		if (score == null || score == 0) {
			oldUserCourseRating.ifPresent(r -> userCourseRatingRepository.deleteById(r.getId()));
			return;
		}

		if (oldUserCourseRating.isEmpty()) {
			userCourseRatingRepository.save(UserCourseRating.builder()
				.courseId(courseId)
				.userId(userId)
				.attributeId(attributeId)
				.score(score)
				.build());
		} else {
			userCourseRatingRepository.save(oldUserCourseRating.get().toBuilder()
				.score(score)
				.build());
		}
	}

	@Transactional(readOnly = true)
	public long countByAlgorithm(Algorithm algorithm, Long tenantId) {
		return courseRepository.countByAlgorithmAndTenantId(algorithm, tenantId);
	}
}

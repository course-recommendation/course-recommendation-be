package com.hcmus.course_recommendation.course.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.dto.AddUserCourseRequest;
import com.hcmus.course_recommendation.course.dto.CourseDetail;
import com.hcmus.course_recommendation.course.dto.GetCourseDetailsRequest;
import com.hcmus.course_recommendation.course.dto.GetCoursesOfUserRequest;
import com.hcmus.course_recommendation.course.dto.GetCoursesRequest;
import com.hcmus.course_recommendation.course.dto.UpdateUserCoursesRequest;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.model.CourseAlgorithm;
import com.hcmus.course_recommendation.course.model.CourseDataset;
import com.hcmus.course_recommendation.course.model.FSCourseExtraData;
import com.hcmus.course_recommendation.course.model.FSItemSentiment;
import com.hcmus.course_recommendation.course.model.UserCourse;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CourseService {
	private final UserCourseRepository userCourseRepository;
	private final CourseRepository courseRepository;

	@Transactional
	public void updateUserCourses(UpdateUserCoursesRequest request) {
		var deletingUserCourseIds = userCourseRepository.findByUserIdAndStatusAndCourseDomain(request.getUserId(),
			request.getUserCourseStatus(), request.getCourseDomain().getAlgorithm(),
			request.getCourseDomain().getDataset()).stream().map(UserCourse::getId).toList();
		userCourseRepository.deleteAllById(deletingUserCourseIds);
		userCourseRepository.saveAll(request.getCourseIds()
			.stream()
			.map(courseId -> UserCourse.builder()
				.status(request.getUserCourseStatus())
				.userId(request.getUserId())
				.courseId(courseId)
				.build())
			.toList());
	}

	@Transactional
	public void addUserCourse(AddUserCourseRequest request) {
		userCourseRepository.save(UserCourse.builder()
			.userId(request.getUserId())
			.courseId(request.getCourseId())
			.status(request.getStatus())
			.build());
	}

	@Transactional
	public void deleteUserCourse(String userId, String courseId) {
		userCourseRepository.deleteByUserIdAndCourseId(userId, courseId);
	}

	@Transactional(readOnly = true)
	public List<CourseDetail> getCoursesOfUser(GetCoursesOfUserRequest request) {
		var userCourses = userCourseRepository.findByUserIdAndStatusAndCourseDomain(request.getUserId(),
			request.getUserCourseStatus(), request.getCourseDomain().getAlgorithm(),
			request.getCourseDomain().getDataset());
		var courseIds = userCourses.stream().map(UserCourse::getCourseId).toList();

		return toCourseDetails(courseIds, request.getUserId());
	}

	@Transactional(readOnly = true)
	public Map<String, CourseDetail> getCourseIdToCourseDetailsByCourseIds(List<String> courseIds, String userId) {
		return toCourseDetails(courseIds, userId).stream()
			.collect(Collectors.toMap(courseDetail -> courseDetail.course().getId(), Function.identity()));
	}

	@Transactional(readOnly = true)
	public List<CourseDetail> toCourseDetails(List<String> courseIds, String userId) {
		var courses = courseRepository.findByIdIn(courseIds);
		var userCourses = userCourseRepository.findByUserId(userId);
		var courseIdToUserCourses = userCourses.stream().collect(Collectors.groupingBy(UserCourse::getCourseId));

		return courses.stream()
			.map(course -> CourseDetail.builder()
				.course(course)
				.userCourseStatuses(courseIdToUserCourses.getOrDefault(course.getId(), List.of())
					.stream()
					.map(UserCourse::getStatus)
					.toList())
				.build())
			.toList();
	}

	@Transactional(readOnly = true)
	public List<CourseDetail> getCourseDetails(GetCourseDetailsRequest request) {
		return toCourseDetails(
			courseRepository.findByAlgorithmAndDataset(request.getDomain().getAlgorithm(), request.getDomain()
				.getDataset()).stream().map(Course::getId).toList(),
			request.getUserId());
	}

	@Transactional(readOnly = true)
	public List<Course> getCourses(GetCoursesRequest request) {
		return courseRepository.findByAlgorithmAndDataset(request.getDomain().getAlgorithm(),
			request.getDomain().getDataset());
	}

	@Transactional(readOnly = true)
	public List<Course> findCoursesByIds(List<String> courseIds) {
		return courseRepository.findByIdIn(courseIds);
	}

	@Transactional(readOnly = true)
	public Map<String, Course> getCourseIdToCourseMapByCourseIds(List<String> courseIds) {
		return findCoursesByIds(courseIds).stream().collect(Collectors.toMap(Course::getId, Function.identity()));
	}

	@Transactional(readOnly = true)
	public Map<String, List<FSItemSentiment>> getFSItemIdToItemSentiments(CourseDataset dataset) {
		return courseRepository.findByAlgorithmAndDataset(CourseAlgorithm.FS, dataset)
			.stream()
			.collect(
				Collectors.toMap(Course::getId, course -> ((FSCourseExtraData)course.getExtraData()).itemSentiments()));
	}
}

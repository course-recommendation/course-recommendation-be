package com.hcmus.course_recommendation.course.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.course.dto.CourseRatingSectionResponse;
import com.hcmus.course_recommendation.course.dto.CourseRatingSectionResponse.CourseAttributeRatingResponse;
import com.hcmus.course_recommendation.course.dto.SaveCourseRatingRequest;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.model.UserCourseRating;
import com.hcmus.course_recommendation.course.model.UserCourseSatisfaction;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseRatingRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseSatisfactionRepository;
import com.hcmus.course_recommendation.course.service.CourseService;
import com.hcmus.course_recommendation.recommendation.model.Attribute;
import com.hcmus.course_recommendation.recommendation.repository.AttributeRepository;
import com.hcmus.course_recommendation.tenant.TenantId;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/course-rating")
@RequiredArgsConstructor
public class CourseRatingController {

	private final UserCourseRatingRepository userCourseRatingRepository;
	private final UserCourseSatisfactionRepository userCourseSatisfactionRepository;
	private final CourseRepository courseRepository;
	private final AttributeRepository attributeRepository;
	private final CourseService courseService;

	@GetMapping
	@Transactional(readOnly = true)
	public RestResponse<List<CourseRatingSectionResponse>> getCourseRatings(Principal principal) {
		var ratings = userCourseRatingRepository.findByUserId(principal.getName());
		var satisfactionByCourseId = userCourseSatisfactionRepository.findByUserId(principal.getName()).stream()
			.filter(satisfaction -> Objects.nonNull(satisfaction.getScore()))
			.collect(Collectors.toMap(UserCourseSatisfaction::getCourseId, UserCourseSatisfaction::getScore,
				(existing, ignored) -> existing));
		if (ratings.isEmpty() && satisfactionByCourseId.isEmpty()) {
			return RestResponse.make(List.of());
		}

		var ratingsByCourse = ratings.stream()
			.collect(Collectors.groupingBy(UserCourseRating::getCourseId));
		// A course belongs in the form if the user rated its attributes OR gave it an overall star, so
		// that a section is not lost when only one of the two was filled in.
		var courseIds = Stream.concat(ratingsByCourse.keySet().stream(), satisfactionByCourseId.keySet().stream())
			.distinct()
			.toList();
		var attributeIds = ratings.stream().map(UserCourseRating::getAttributeId).distinct().toList();

		Map<Long, Course> courseById = courseRepository.findByIdIn(courseIds).stream()
			.collect(Collectors.toMap(Course::getId, c -> c));
		Map<Long, Attribute> attributeById = attributeRepository.findAllById(attributeIds).stream()
			.collect(Collectors.toMap(Attribute::getId, a -> a));

		var sections = courseIds.stream()
			.filter(courseById::containsKey)
			.map(courseId -> {
				var course = courseById.get(courseId);
				var attrRatings = ratingsByCourse.getOrDefault(courseId, List.of()).stream()
					.filter(r -> attributeById.containsKey(r.getAttributeId()))
					.map(r -> {
						var attr = attributeById.get(r.getAttributeId());
						return new CourseAttributeRatingResponse(attr.getId(), attr.getValue(), r.getScore());
					})
					.toList();
				return new CourseRatingSectionResponse(course.getId(), course.getCode(), course.getName(),
					CourseService.toStarScore(satisfactionByCourseId.get(courseId)), attrRatings);
			})
			.toList();

		return RestResponse.make(sections);
	}

	@PostMapping
	@Transactional
	public RestResponse<Void> saveCourseRatings(
		@RequestBody SaveCourseRatingRequest request,
		Principal principal,
		@TenantId Long tenantId) {

		for (var section : request.sections()) {
			// A null satisfaction means "not supplied", which must not wipe an existing score - only an
			// explicit 0 clears it. Older clients that do not know about the field therefore keep working.
			if (section.satisfaction() != null) {
				courseService.rateCourseSatisfaction(
					principal.getName(), tenantId,
					section.courseId(), section.satisfaction());
			}
			for (var ar : section.attributeRatings()) {
				courseService.rateCourse(
					principal.getName(), tenantId,
					section.courseId(), ar.attributeId(), ar.score());
			}
		}

		return RestResponse.make();
	}
}

package com.hcmus.course_recommendation.course.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseRatingRepository;
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
	private final CourseRepository courseRepository;
	private final AttributeRepository attributeRepository;
	private final CourseService courseService;

	@GetMapping
	@Transactional(readOnly = true)
	public RestResponse<List<CourseRatingSectionResponse>> getCourseRatings(Principal principal) {
		var ratings = userCourseRatingRepository.findByUserId(principal.getName());
		if (ratings.isEmpty()) {
			return RestResponse.make(List.of());
		}

		var courseIds = ratings.stream().map(r -> r.getCourseId()).distinct().toList();
		var attributeIds = ratings.stream().map(r -> r.getAttributeId()).distinct().toList();

		Map<Long, Course> courseById = courseRepository.findByIdIn(courseIds).stream()
			.collect(Collectors.toMap(Course::getId, c -> c));
		Map<Long, Attribute> attributeById = attributeRepository.findAllById(attributeIds).stream()
			.collect(Collectors.toMap(Attribute::getId, a -> a));

		var ratingsByCourse = ratings.stream()
			.collect(Collectors.groupingBy(r -> r.getCourseId()));

		var sections = ratingsByCourse.entrySet().stream()
			.filter(e -> courseById.containsKey(e.getKey()))
			.map(e -> {
				var course = courseById.get(e.getKey());
				var attrRatings = e.getValue().stream()
					.filter(r -> attributeById.containsKey(r.getAttributeId()))
					.map(r -> {
						var attr = attributeById.get(r.getAttributeId());
						return new CourseAttributeRatingResponse(attr.getId(), attr.getValue(), r.getScore());
					})
					.toList();
				return new CourseRatingSectionResponse(
					course.getId(), course.getCode(), course.getName(), attrRatings);
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
			for (var ar : section.attributeRatings()) {
				courseService.rateCourse(
					principal.getName(), tenantId,
					section.courseId(), ar.attributeId(), ar.score());
			}
		}

		return RestResponse.make();
	}
}

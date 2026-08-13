package com.hcmus.course_recommendation.course.controller;

import java.security.Principal;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.course.dto.CourseDetail;
import com.hcmus.course_recommendation.course.dto.GetCourseDetailRequest;
import com.hcmus.course_recommendation.course.dto.GetCourseDetailsRequest;
import com.hcmus.course_recommendation.course.dto.GetCoursesOfUserRequest;
import com.hcmus.course_recommendation.course.dto.GetCoursesRequest;
import com.hcmus.course_recommendation.course.dto.RateCourseRequest;
import com.hcmus.course_recommendation.course.dto.RateCourseSatisfactionRequest;
import com.hcmus.course_recommendation.course.dto.UpdateUserCourseStatusRequest;
import com.hcmus.course_recommendation.course.dto.UpdateUserCourseStatusesRequest;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.service.CourseService;
import com.hcmus.course_recommendation.tenant.TenantId;

@RestController
public class CourseController {
	private final CourseService courseService;

	public CourseController(CourseService courseService) {
		this.courseService = courseService;
	}

	@PutMapping("/me/courses")
	public RestResponse<Void> updateUserCourses(@RequestBody UpdateUserCourseStatusesRequest request,
		Principal principal, @TenantId Long tenantId) {
		request.setUserId(principal.getName());
		request.setTenantId(tenantId);
		courseService.updateUserCourseStatuses(request);

		return RestResponse.make();
	}

	@PutMapping("/me/courses/{courseId}")
	public RestResponse<Void> updateUserCourse(@RequestBody UpdateUserCourseStatusRequest request,
		@PathVariable Long courseId, Principal principal, @TenantId Long tenantId) {
		request.setUserId(principal.getName());
		request.setCourseId(courseId);
		request.setTenantId(tenantId);
		courseService.updateUserCourseStatus(request);

		return RestResponse.make();
	}

	@GetMapping("/me/courses")
	public RestResponse<List<CourseDetail>> getCoursesOfUser(GetCoursesOfUserRequest request, Principal principal,
		@TenantId Long tenantId) {
		request.setUserId(principal.getName());
		request.setTenantId(tenantId);
		return RestResponse.make(courseService.getCoursesOfUser(request));
	}

	@GetMapping("/courses/detail")
	public RestResponse<List<CourseDetail>> getCourseDetails(GetCourseDetailsRequest request, Principal principal,
		@TenantId Long tenantId) {
		request.setUserId(principal.getName());
		request.setTenantId(tenantId);

		return RestResponse.make(courseService.getCourseDetails(request));
	}

	@GetMapping("/courses")
	public RestResponse<List<Course>> getAllCourses(@ParameterObject GetCoursesRequest request,
		@TenantId Long tenantId) {
		request.setTenantId(tenantId);
		return RestResponse.make(courseService.getCourses(request));
	}

	@GetMapping("/courses/{courseCode}/detail")
	public RestResponse<CourseDetail> getCourseDetails(@PathVariable String courseCode,
		@ParameterObject GetCourseDetailRequest request, Principal principal, @TenantId Long tenantId) {
		request.setUserId(principal.getName());
		request.setCourseCode(courseCode);
		request.setTenantId(tenantId);

		return RestResponse.make(courseService.getCourseDetail(request));
	}

	@PutMapping("/courses/{courseId}/rating")
	public RestResponse<Void> rateCourse(@PathVariable Long courseId, Principal principal, @TenantId Long tenantId,
		@RequestBody RateCourseRequest request) {
		courseService.rateCourse(principal.getName(), tenantId, courseId, request.getAttributeId(),
			request.getScore());

		return RestResponse.make();
	}

	@PutMapping("/courses/{courseId}/satisfaction")
	public RestResponse<Void> rateCourseSatisfaction(@PathVariable Long courseId, Principal principal,
		@TenantId Long tenantId, @RequestBody RateCourseSatisfactionRequest request) {
		courseService.rateCourseSatisfaction(principal.getName(), tenantId, courseId, request.getScore());

		return RestResponse.make();
	}
}

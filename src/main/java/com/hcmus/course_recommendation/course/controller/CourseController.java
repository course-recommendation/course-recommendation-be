package com.hcmus.course_recommendation.course.controller;

import java.security.Principal;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.course.dto.AddUserCourseRequest;
import com.hcmus.course_recommendation.course.dto.CourseDetail;
import com.hcmus.course_recommendation.course.dto.GetCourseDetailsRequest;
import com.hcmus.course_recommendation.course.dto.GetCoursesOfUserRequest;
import com.hcmus.course_recommendation.course.dto.GetCoursesRequest;
import com.hcmus.course_recommendation.course.dto.UpdateUserCoursesRequest;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.service.CourseService;

@RestController
public class CourseController {
	private final CourseService courseService;

	public CourseController(CourseService courseService) {
		this.courseService = courseService;
	}

	@PutMapping("/me/courses")
	public RestResponse<Void> updateUserCourses(@RequestBody UpdateUserCoursesRequest request, Principal principal) {
		request.setUserId(principal.getName());
		courseService.updateUserCourses(request);

		return RestResponse.make();
	}

	@PostMapping("/me/courses")
	public RestResponse<Void> addUserCourse(@RequestBody AddUserCourseRequest request, Principal principal) {
		request.setUserId(principal.getName());
		courseService.addUserCourse(request);

		return RestResponse.make();
	}

	@DeleteMapping("/me/courses/{courseId}")
	public RestResponse<Void> deleteUserCourse(@PathVariable String courseId, Principal principal) {
		courseService.deleteUserCourse(principal.getName(), courseId);

		return RestResponse.make();
	}

	@GetMapping("/me/courses")
	public RestResponse<List<CourseDetail>> getCoursesOfUser(GetCoursesOfUserRequest request, Principal principal) {
		request.setUserId(principal.getName());
		return RestResponse.make(courseService.getCoursesOfUser(request));
	}

	@GetMapping("/courses/detail")
	public RestResponse<List<CourseDetail>> getCourseDetails(GetCourseDetailsRequest request, Principal principal) {
		request.setUserId(principal.getName());

		return RestResponse.make(courseService.getCourseDetails(request));
	}

	@GetMapping("/courses")
	public RestResponse<List<Course>> getAllCourses(@ParameterObject GetCoursesRequest request) {
		return RestResponse.make(courseService.getCourses(request));
	}
}

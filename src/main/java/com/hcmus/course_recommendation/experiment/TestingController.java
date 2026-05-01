package com.hcmus.course_recommendation.experiment;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.recommendation.tri_rank.service.TriRankService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestingController {
	private final CourseRepository courseRepository;
	private final TriRankService triRankService;

	@GetMapping
	public List<String> test() {
		return courseRepository.findAll().stream().map(Course::getCode).toList();
	}

	@PostMapping("/trirank/export")
	public RestResponse<Void> exportTriRankDataset() {
		triRankService.exportTriRankDatasetToAzure();
		return RestResponse.make();
	}
}

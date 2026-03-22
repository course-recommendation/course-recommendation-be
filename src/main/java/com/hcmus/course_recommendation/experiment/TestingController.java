package com.hcmus.course_recommendation.experiment;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.course.model.Dataset;
import com.hcmus.course_recommendation.course.repository.CourseRepository;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestingController {
	private final CourseRepository courseRepository;

	@GetMapping
	public List<Course2> test() {
		return courseRepository.findByDataset(Dataset.FIT).stream().map(course -> Course2.builder()
			.id(course.getCode())
			.name(course.getName())
			.build()).toList();
	}

	@Builder
	public record Course2(
		String id,
		String name
	) {
	}
}

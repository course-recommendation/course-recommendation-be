package com.hcmus.course_recommendation.recommendation.fs.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hcmus.course_recommendation.course.model.Dataset;

@Component
public class FsScheduler {
	private final FSService fSService;

	public FsScheduler(FSService fSService) {
		this.fSService = fSService;
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void updateCoursesSentiment() {
		for (var dataset : Dataset.values()) {
			fSService.updateCoursesSentiments(dataset);
		}
	}
}

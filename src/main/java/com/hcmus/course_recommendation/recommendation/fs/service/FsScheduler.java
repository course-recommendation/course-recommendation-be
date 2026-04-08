package com.hcmus.course_recommendation.recommendation.fs.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FsScheduler {
	private final FSService fSService;

	public FsScheduler(FSService fSService) {
		this.fSService = fSService;
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void updateCoursesSentiment() {
		fSService.updateCoursesSentiments();
	}
}

package com.hcmus.course_recommendation.recommendation.fs.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hcmus.course_recommendation.tenant.TenantRepository;

@Component
public class FsScheduler {
	private final FSService fSService;
	private final TenantRepository tenantRepository;

	public FsScheduler(FSService fSService, TenantRepository tenantRepository) {
		this.fSService = fSService;
		this.tenantRepository = tenantRepository;
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void updateCoursesSentiment() {
		tenantRepository.findAll().forEach(tenant -> fSService.updateCoursesSentiments(tenant.getId()));
	}
}

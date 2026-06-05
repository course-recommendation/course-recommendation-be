package com.hcmus.course_recommendation.recommendation.admin;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hcmus.course_recommendation.recommendation.fs.service.FSService;
import com.hcmus.course_recommendation.recommendation.tri_rank.service.TriRankService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetrainService {

	private final TriRankService triRankService;
	private final FSService fSService;

	@Async
	public void retrainAsync(Long tenantId) {
		try {
			triRankService.trainTriRank(tenantId);
		} catch (Exception e) {
			log.warn("TriRank retrain failed for tenant {}", tenantId, e);
		}
		try {
			fSService.updateCoursesSentiments(tenantId);
		} catch (Exception e) {
			log.warn("FS retrain failed for tenant {}", tenantId, e);
		}
	}
}

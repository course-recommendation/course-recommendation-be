package com.hcmus.course_recommendation.experiment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.recommendation.tri_rank.service.TriRankService;
import com.hcmus.course_recommendation.tenant.TenantId;

import jakarta.servlet.http.HttpServletRequest;
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
	public String test(HttpServletRequest servletRequest, @TenantId Long tenantId) {
		return servletRequest.getServerName();
	}

	@PostMapping("/trirank/export")
	public RestResponse<Void> exportTriRankDataset(@TenantId Long tenantId) {
		triRankService.exportTriRankDatasetToAzure(tenantId);
		return RestResponse.make();
	}
}

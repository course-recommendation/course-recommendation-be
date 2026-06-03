package com.hcmus.course_recommendation.tenant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.tenant.TenantId;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TenantController {
	private final TenantRepository tenantRepository;

	@GetMapping("/tenant/algorithm")
	public RestResponse<Algorithm> getTenantAlgorithm(@TenantId Long tenantId) {
		Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
		if (tenant == null || tenant.getAlgorithm() == null) {
			return RestResponse.make(Algorithm.FS);
		}
		return RestResponse.make(tenant.getAlgorithm());
	}
}

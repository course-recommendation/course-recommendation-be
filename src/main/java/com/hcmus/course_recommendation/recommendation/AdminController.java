package com.hcmus.course_recommendation.recommendation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.common.exception.BadRequestException;
import com.hcmus.course_recommendation.common.exception.GlobalErrorCode;
import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.recommendation.model.Attribute;
import com.hcmus.course_recommendation.recommendation.repository.AttributeRepository;
import com.hcmus.course_recommendation.recommendation.tri_rank.service.TriRankService;
import com.hcmus.course_recommendation.tenant.TenantId;
import com.hcmus.course_recommendation.user.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {
	private final CourseRepository courseRepository;
	private final AttributeRepository attributeRepository;
	private final UserService userService;
	private final TriRankService triRankService;

	/**
	 * Check if current tenant is ready: at least 1 course and at least 1 attribute
	 */
	@GetMapping("/tenant-ready")
	public RestResponse<Boolean> isTenantReady(@TenantId Long tenantId) {
		long courses = courseRepository.countByTenantId(tenantId);
		long attributes = attributeRepository.countByTenantId(tenantId);
		return RestResponse.make(courses > 0 && attributes > 0);
	}

	/**
	 * Upload CSV file with courses. CSV expected: code,name,description (header optional)
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping(value = "/courses/upload", consumes = {"multipart/form-data"})
	public RestResponse<Void> uploadCourses(@RequestParam("file") MultipartFile file,
		@TenantId Long tenantId, Principal principal) {
		try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
			List<Course> toSave = new ArrayList<>();
			List<String> lines = reader.lines().toList();
			int start = 0;
			if (!lines.isEmpty() && lines.getFirst().toLowerCase().contains("code")) {
				start = 1;
			}
			for (int i = start; i < lines.size(); i++) {
				String line = lines.get(i).trim();
				if (line.isEmpty())
					continue;
				String[] cols = line.split(";");
				String code = cols.length > 0 ? cols[0].trim() : null;
				String name = cols.length > 1 ? cols[1].trim() : null;
				String description = cols.length > 2 ? cols[2].trim() : null;
				if (code == null || name == null)
					continue;
				Course course = Course.builder()
					.code(code)
					.name(name)
					.description(description)
					.tenantId(tenantId)
					.algorithm(Algorithm.FS)
					.thumbnailUrl(String.format("https://picsum.photos/seed/%s/1600/900", code))
					.build();
				toSave.add(course);
			}

			courseRepository.saveAll(toSave);
			return RestResponse.make();
		} catch (Exception e) {
			throw new BadRequestException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Save attributes for tenant. Only allowed when there are no attributes yet for this tenant and algorithm FS.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/attributes")
	public RestResponse<Void> saveAttributes(@RequestBody List<String> values, @TenantId Long tenantId) {
		long existing = attributeRepository.countByAlgorithmAndTenantId(Algorithm.FS, tenantId);
		if (existing > 0) {
			throw new BadRequestException(GlobalErrorCode.BAD_REQUEST);
		}

		List<Attribute> attrs = values.stream().filter(v -> v != null && !v.trim().isEmpty())
			.map(v -> Attribute.builder().algorithm(Algorithm.FS).tenantId(tenantId).value(v.trim()).build())
			.collect(Collectors.toList());

		attributeRepository.saveAll(attrs);
		return RestResponse.make();
	}

	/**
	 * Check if current user is admin
	 */
	@GetMapping("/is-admin")
	public RestResponse<Boolean> isAdmin(Principal principal) {
		var user = userService.getUserById(principal.getName());
		if (user == null)
			return RestResponse.make(false);
		return RestResponse.make(
			user.getRoles() != null && user.getRoles().stream().anyMatch(r -> r.name().equals("ADMIN")));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/trirank/train")
	public RestResponse<Void> trainTriRankForMyTenant(@TenantId Long tenantId) {
		triRankService.trainTriRank(tenantId);
		return RestResponse.make();
	}

	@PostMapping("/trirank/{tenantId}/train")
	public RestResponse<Void> trainTriRank(@PathVariable Long tenantId) {
		triRankService.trainTriRank(tenantId);
		return RestResponse.make();
	}
}

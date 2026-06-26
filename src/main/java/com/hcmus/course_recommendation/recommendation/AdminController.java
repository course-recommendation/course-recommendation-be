package com.hcmus.course_recommendation.recommendation;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hcmus.course_recommendation.common.RestResponse;
import com.hcmus.course_recommendation.common.dto.PageResponse;
import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.recommendation.admin.AdminService;
import com.hcmus.course_recommendation.recommendation.admin.dto.AdminAttributeRow;
import com.hcmus.course_recommendation.recommendation.admin.dto.AdminCourseRow;
import com.hcmus.course_recommendation.recommendation.admin.dto.AdminRatingRow;
import com.hcmus.course_recommendation.recommendation.admin.dto.AdminUserRow;
import com.hcmus.course_recommendation.recommendation.admin.dto.BulkDeleteRequest;
import com.hcmus.course_recommendation.recommendation.admin.dto.CreateAdminUserRequest;
import com.hcmus.course_recommendation.recommendation.admin.dto.CreateRatingRequest;
import com.hcmus.course_recommendation.recommendation.admin.dto.UpdateAdminUserRequest;
import com.hcmus.course_recommendation.recommendation.admin.dto.UpdateRatingRequest;
import com.hcmus.course_recommendation.recommendation.admin.dto.UpsertAttributeRequest;
import com.hcmus.course_recommendation.recommendation.admin.dto.UpsertCourseRequest;
import com.hcmus.course_recommendation.recommendation.fs.service.FSService;
import com.hcmus.course_recommendation.recommendation.tri_rank.service.TriRankService;
import com.hcmus.course_recommendation.tenant.TenantId;
import com.hcmus.course_recommendation.user.Role;
import com.hcmus.course_recommendation.user.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {

	private final UserService userService;
	private final TriRankService triRankService;
	private final FSService fSService;
	private final AdminService adminService;

	// ─── Tenant / Auth ────────────────────────────────────────────────────────

	@GetMapping("/tenant-ready")
	public RestResponse<Boolean> isTenantReady(@TenantId Long tenantId) {
		return RestResponse.make(
			adminService.getCourses(tenantId, Pageable.ofSize(1), null, null, null).totalElements() > 0
				&& adminService.getAttributes(tenantId, Pageable.ofSize(1), null, null).totalElements() > 0);
	}

	@GetMapping("/is-admin")
	public RestResponse<Boolean> isAdmin(Principal principal) {
		var user = userService.getUserById(principal.getName());
		if (user == null)
			return RestResponse.make(false);
		return RestResponse.make(
			user.getRoles() != null && user.getRoles().stream().anyMatch(r -> r.name().equals("ADMIN")));
	}

	// ─── Train ────────────────────────────────────────────────────────────────

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

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/fs/train")
	public RestResponse<Void> trainFsForMyTenant(@TenantId Long tenantId) {
		fSService.updateCoursesSentiments(tenantId);
		return RestResponse.make();
	}

	// ─── Users ────────────────────────────────────────────────────────────────

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/users")
	public RestResponse<PageResponse<AdminUserRow>> getUsers(
		@TenantId Long tenantId,
		@PageableDefault(size = 10) Pageable pageable,
		@RequestParam(required = false) String email,
		@RequestParam(required = false) String fullName,
		@RequestParam(required = false) List<Role> roles) {
		return RestResponse.make(adminService.getUsers(tenantId, pageable, email, fullName, roles));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/users")
	public RestResponse<Void> createUser(@TenantId Long tenantId, @RequestBody CreateAdminUserRequest request) {
		adminService.createUser(tenantId, request);
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/users/{id}")
	public RestResponse<Void> updateUser(@TenantId Long tenantId, @PathVariable String id,
		@RequestBody UpdateAdminUserRequest request) {
		adminService.updateUser(tenantId, id, request);
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/users/{id}")
	public RestResponse<Void> deleteUser(@TenantId Long tenantId, @PathVariable String id) {
		adminService.deleteUser(tenantId, id);
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/users/bulk-delete")
	public RestResponse<Void> bulkDeleteUsers(@TenantId Long tenantId, @RequestBody BulkDeleteRequest request) {
		adminService.deleteUsers(tenantId, request.ids());
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping(value = "/users/import", consumes = "multipart/form-data")
	public RestResponse<Void> importUsers(@TenantId Long tenantId,
		@RequestParam("file") MultipartFile file) throws Exception {
		adminService.importUsers(tenantId, file);
		return RestResponse.make();
	}

	// ─── Attributes ───────────────────────────────────────────────────────────

	@GetMapping("/attributes")
	public RestResponse<PageResponse<AdminAttributeRow>> getAttributes(
		@TenantId Long tenantId,
		@PageableDefault(size = 10) Pageable pageable,
		@RequestParam(required = false) String value,
		@RequestParam(required = false) Algorithm algorithm) {
		return RestResponse.make(adminService.getAttributes(tenantId, pageable, value, algorithm));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/attributes/create")
	public RestResponse<Void> createAttribute(@TenantId Long tenantId,
		@RequestBody UpsertAttributeRequest request) {
		adminService.createAttribute(tenantId, request);
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/attributes/{id}")
	public RestResponse<Void> updateAttribute(@TenantId Long tenantId, @PathVariable Long id,
		@RequestBody UpsertAttributeRequest request) {
		adminService.updateAttribute(tenantId, id, request);
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/attributes/{id}")
	public RestResponse<Void> deleteAttribute(@TenantId Long tenantId, @PathVariable Long id) {
		adminService.deleteAttribute(tenantId, id);
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/attributes/bulk-delete")
	public RestResponse<Void> bulkDeleteAttributes(@TenantId Long tenantId,
		@RequestBody BulkDeleteRequest request) {
		adminService.deleteAttributes(tenantId, request.ids().stream().map(Long::parseLong).toList());
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping(value = "/attributes/import", consumes = "multipart/form-data")
	public RestResponse<Void> importAttributes(@TenantId Long tenantId,
		@RequestParam("file") MultipartFile file) throws Exception {
		adminService.importAttributes(tenantId, file);
		return RestResponse.make();
	}

	// ─── Courses ──────────────────────────────────────────────────────────────

	@GetMapping("/courses")
	public RestResponse<PageResponse<AdminCourseRow>> getCourses(
		@TenantId Long tenantId,
		@PageableDefault(size = 10) Pageable pageable,
		@RequestParam(required = false) String code,
		@RequestParam(required = false) String name,
		@RequestParam(required = false) Algorithm algorithm) {
		return RestResponse.make(adminService.getCourses(tenantId, pageable, code, name, algorithm));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/courses")
	public RestResponse<Void> createCourse(@TenantId Long tenantId, @RequestBody UpsertCourseRequest request) {
		adminService.createCourse(tenantId, request);
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/courses/{id}")
	public RestResponse<Void> updateCourse(@TenantId Long tenantId, @PathVariable Long id,
		@RequestBody UpsertCourseRequest request) {
		adminService.updateCourse(tenantId, id, request);
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/courses/{id}")
	public RestResponse<Void> deleteCourse(@TenantId Long tenantId, @PathVariable Long id) {
		adminService.deleteCourse(tenantId, id);
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/courses/bulk-delete")
	public RestResponse<Void> bulkDeleteCourses(@TenantId Long tenantId, @RequestBody BulkDeleteRequest request) {
		adminService.deleteCourses(tenantId, request.ids().stream().map(Long::parseLong).toList());
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping(value = "/courses/import", consumes = "multipart/form-data")
	public RestResponse<Void> importCourses(@TenantId Long tenantId,
		@RequestParam("file") MultipartFile file) throws Exception {
		adminService.importCourses(tenantId, file);
		return RestResponse.make();
	}

	// ─── Ratings ──────────────────────────────────────────────────────────────

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/ratings")
	public RestResponse<PageResponse<AdminRatingRow>> getRatings(
		@TenantId Long tenantId,
		@PageableDefault(size = 10) Pageable pageable,
		@RequestParam(required = false) String userEmail,
		@RequestParam(required = false) String courseCode,
		@RequestParam(required = false) String attributeName) {
		return RestResponse.make(adminService.getRatings(tenantId, pageable, userEmail, courseCode, attributeName));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/ratings")
	public RestResponse<Void> createRating(@TenantId Long tenantId, @RequestBody CreateRatingRequest request) {
		adminService.createRating(tenantId, request);
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/ratings/{id}")
	public RestResponse<Void> updateRating(@TenantId Long tenantId, @PathVariable Long id,
		@RequestBody UpdateRatingRequest request) {
		adminService.updateRating(tenantId, id, request);
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/ratings/{id}")
	public RestResponse<Void> deleteRating(@TenantId Long tenantId, @PathVariable Long id) {
		adminService.deleteRating(tenantId, id);
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/ratings/bulk-delete")
	public RestResponse<Void> bulkDeleteRatings(@TenantId Long tenantId, @RequestBody BulkDeleteRequest request) {
		adminService.deleteRatings(tenantId, request.ids().stream().map(Long::parseLong).toList());
		return RestResponse.make();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping(value = "/ratings/import", consumes = "multipart/form-data")
	public RestResponse<Void> importRatings(@TenantId Long tenantId,
		@RequestParam("file") MultipartFile file) throws Exception {
		adminService.importRatings(tenantId, file);
		return RestResponse.make();
	}
}

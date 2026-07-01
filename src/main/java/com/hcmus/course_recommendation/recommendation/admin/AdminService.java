package com.hcmus.course_recommendation.recommendation.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.hcmus.course_recommendation.common.dto.PageResponse;
import com.hcmus.course_recommendation.common.event.DeletedResourceApplicationEvent;
import com.hcmus.course_recommendation.common.exception.BadRequestException;
import com.hcmus.course_recommendation.common.exception.GlobalErrorCode;
import com.hcmus.course_recommendation.common.exception.NotFoundException;
import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.model.UserCourseRating;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseRatingRepository;
import com.hcmus.course_recommendation.recommendation.admin.dto.AdminAttributeRow;
import com.hcmus.course_recommendation.recommendation.admin.dto.AdminCourseRow;
import com.hcmus.course_recommendation.recommendation.admin.dto.AdminRatingRow;
import com.hcmus.course_recommendation.recommendation.admin.dto.AdminUserRow;
import com.hcmus.course_recommendation.recommendation.admin.dto.CreateAdminUserRequest;
import com.hcmus.course_recommendation.recommendation.admin.dto.CreateRatingRequest;
import com.hcmus.course_recommendation.recommendation.admin.dto.UpdateAdminUserRequest;
import com.hcmus.course_recommendation.recommendation.admin.dto.UpdateRatingRequest;
import com.hcmus.course_recommendation.recommendation.admin.dto.UpsertAttributeRequest;
import com.hcmus.course_recommendation.recommendation.admin.dto.UpsertCourseRequest;
import com.hcmus.course_recommendation.recommendation.model.Attribute;
import com.hcmus.course_recommendation.recommendation.repository.AttributeRepository;
import com.hcmus.course_recommendation.user.Role;
import com.hcmus.course_recommendation.user.User;
import com.hcmus.course_recommendation.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

	private final UserRepository userRepository;
	private final CourseRepository courseRepository;
	private final AttributeRepository attributeRepository;
	private final UserCourseRatingRepository ratingRepository;
	private final PasswordEncoder passwordEncoder;
	private final ApplicationEventPublisher eventPublisher;
	private final RetrainService retrainService;

	// ─── Users ────────────────────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public PageResponse<AdminUserRow> getUsers(Long tenantId, Pageable pageable, String email, String fullName,
		List<Role> roles) {
		Specification<User> spec = (root, query, cb) -> {
			var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
			predicates.add(cb.equal(root.get("tenantId"), tenantId));
			if (email != null && !email.isBlank())
				predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
			if (fullName != null && !fullName.isBlank())
				predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%"));
			if (roles != null && !roles.isEmpty()) {
				var rolePredicates = roles.stream()
					.map(role -> cb.isTrue(cb.function("JSON_CONTAINS", Boolean.class,
						root.get("roles"), cb.literal("\"" + role.name() + "\""))))
					.toArray(jakarta.persistence.criteria.Predicate[]::new);
				predicates.add(cb.or(rolePredicates));
			}
			return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
		};
		Page<User> page = userRepository.findAll(spec, pageable);
		return PageResponse.from(page.map(u -> new AdminUserRow(u.getId(), u.getEmail(), u.getFullName(),
			u.getRoles(), u.getCreatedAt())));
	}

	@Transactional
	public void createUser(Long tenantId, CreateAdminUserRequest request) {
		if (userRepository.findByEmailAndTenantId(request.getEmail(), tenantId).isPresent()) {
			throw new BadRequestException(GlobalErrorCode.EMAIL_DUPLICATED);
		}
		var roles = request.getRoles() != null && !request.getRoles().isEmpty()
			? request.getRoles() : List.of(Role.USER);
		var user = User.builder()
			.email(request.getEmail())
			.password(passwordEncoder.encode(request.getPassword()))
			.fullName(request.getFullName())
			.tenantId(tenantId)
			.roles(roles)
			.build();
		userRepository.save(user);
	}

	@Transactional
	public void updateUser(Long tenantId, String userId, UpdateAdminUserRequest request) {
		User user = userRepository.findById(userId)
			.filter(u -> tenantId.equals(u.getTenantId()))
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.USER_NOT_FOUND));
		if (request.getEmail() != null)
			user.setEmail(request.getEmail());
		if (request.getFullName() != null)
			user.setFullName(request.getFullName());
		if (request.getRoles() != null && !request.getRoles().isEmpty())
			user.setRoles(request.getRoles());
		userRepository.save(user);
	}

	@Transactional
	public void deleteUser(Long tenantId, String userId) {
		User user = userRepository.findById(userId)
			.filter(u -> tenantId.equals(u.getTenantId()))
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.USER_NOT_FOUND));
		userRepository.delete(user);
		eventPublisher.publishEvent(new DeletedResourceApplicationEvent<>(this, user));
	}

	@Transactional
	public void deleteUsers(Long tenantId, List<String> userIds) {
		userIds.forEach(id -> deleteUser(tenantId, id));
	}

	@Transactional
	public void importUsers(Long tenantId, MultipartFile file) throws IOException {
		try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0);
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null)
					continue;
				String email = getCellAsString(row.getCell(0));
				String fullName = getCellAsString(row.getCell(1));
				String roleStr = getCellAsString(row.getCell(2));
				String password = getCellAsString(row.getCell(3));
				if (email == null || password == null)
					continue;
				var roles = parseRole(roleStr);
				var req = new CreateAdminUserRequest(email, password, fullName, roles);
				try {
					createUser(tenantId, req);
				} catch (BadRequestException e) {
					log.warn("Skipping duplicate email {} on row {}", email, i);
				}
			}
		}
	}

	// ─── Attributes ───────────────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public PageResponse<AdminAttributeRow> getAttributes(Long tenantId, Pageable pageable, String value,
		Algorithm algorithm) {
		Specification<Attribute> spec = (root, query, cb) -> {
			var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
			predicates.add(cb.equal(root.get("tenantId"), tenantId));
			if (value != null && !value.isBlank())
				predicates.add(cb.like(cb.lower(root.get("value")), "%" + value.toLowerCase() + "%"));
			if (algorithm != null)
				predicates.add(cb.equal(root.get("algorithm"), algorithm));
			return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
		};
		Page<Attribute> page = attributeRepository.findAll(spec, pageable);
		return PageResponse.from(page.map(a -> new AdminAttributeRow(a.getId(), a.getValue(), a.getAlgorithm(),
			a.getCreatedAt())));
	}

	@Transactional
	public void createAttribute(Long tenantId, UpsertAttributeRequest request) {
		var attr = Attribute.builder()
			.value(request.getValue())
			.algorithm(request.getAlgorithm() != null ? request.getAlgorithm() : Algorithm.FS)
			.tenantId(tenantId)
			.build();
		attributeRepository.save(attr);
		scheduleRetrainAfterCommit(tenantId);
	}

	@Transactional
	public void updateAttribute(Long tenantId, Long attrId, UpsertAttributeRequest request) {
		Attribute attr = attributeRepository.findById(attrId)
			.filter(a -> tenantId.equals(a.getTenantId()))
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.NOT_FOUND));
		if (request.getValue() != null)
			attr.setValue(request.getValue());
		if (request.getAlgorithm() != null)
			attr.setAlgorithm(request.getAlgorithm());
		attributeRepository.save(attr);
		scheduleRetrainAfterCommit(tenantId);
	}

	@Transactional
	public void deleteAttribute(Long tenantId, Long attrId) {
		Attribute attr = attributeRepository.findById(attrId)
			.filter(a -> tenantId.equals(a.getTenantId()))
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.NOT_FOUND));
		attributeRepository.delete(attr);
		eventPublisher.publishEvent(new DeletedResourceApplicationEvent<>(this, attr));
	}

	@Transactional
	public void deleteAttributes(Long tenantId, List<Long> ids) {
		ids.forEach(id -> deleteAttribute(tenantId, id));
	}

	@Transactional
	public void importAttributes(Long tenantId, MultipartFile file) throws IOException {
		try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0);
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null)
					continue;
				String value = getCellAsString(row.getCell(0));
				String algorithmStr = getCellAsString(row.getCell(1));
				if (value == null)
					continue;
				Algorithm algorithm = parseAlgorithm(algorithmStr);
				createAttribute(tenantId, new UpsertAttributeRequest(value, algorithm));
			}
		}
	}

	// ─── Courses ──────────────────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public PageResponse<AdminCourseRow> getCourses(Long tenantId, Pageable pageable, String code, String name,
		Algorithm algorithm) {
		Specification<Course> spec = (root, query, cb) -> {
			var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
			predicates.add(cb.equal(root.get("tenantId"), tenantId));
			if (code != null && !code.isBlank())
				predicates.add(cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
			if (name != null && !name.isBlank())
				predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
			if (algorithm != null)
				predicates.add(cb.equal(root.get("algorithm"), algorithm));
			return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
		};
		Page<Course> page = courseRepository.findAll(spec, pageable);
		return PageResponse.from(page.map(c -> new AdminCourseRow(c.getId(), c.getCode(), c.getName(),
			c.getDescription(), c.getAlgorithm(), c.getCreatedAt())));
	}

	@Transactional
	public void createCourse(Long tenantId, UpsertCourseRequest request) {
		var course = Course.builder()
			.code(request.getCode())
			.name(request.getName())
			.description(request.getDescription())
			.algorithm(request.getAlgorithm() != null ? request.getAlgorithm() : Algorithm.FS)
			.tenantId(tenantId)
			.build();
		courseRepository.save(course);
		scheduleRetrainAfterCommit(tenantId);
	}

	@Transactional
	public void updateCourse(Long tenantId, Long courseId, UpsertCourseRequest request) {
		Course course = courseRepository.findById(courseId)
			.filter(c -> tenantId.equals(c.getTenantId()))
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.NOT_FOUND));
		if (request.getCode() != null)
			course.setCode(request.getCode());
		if (request.getName() != null)
			course.setName(request.getName());
		if (request.getDescription() != null)
			course.setDescription(request.getDescription());
		if (request.getAlgorithm() != null)
			course.setAlgorithm(request.getAlgorithm());
		courseRepository.save(course);
	}

	@Transactional
	public void deleteCourse(Long tenantId, Long courseId) {
		Course course = courseRepository.findById(courseId)
			.filter(c -> tenantId.equals(c.getTenantId()))
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.NOT_FOUND));
		courseRepository.delete(course);
		eventPublisher.publishEvent(new DeletedResourceApplicationEvent<>(this, course));
	}

	@Transactional
	public void deleteCourses(Long tenantId, List<Long> ids) {
		ids.forEach(id -> deleteCourse(tenantId, id));
	}

	@Transactional
	public void importCourses(Long tenantId, MultipartFile file) throws IOException {
		try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0);
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null)
					continue;
				String code = getCellAsString(row.getCell(0));
				String name = getCellAsString(row.getCell(1));
				String description = getCellAsString(row.getCell(2));
				String algorithmStr = getCellAsString(row.getCell(3));
				if (code == null || name == null)
					continue;
				Algorithm algorithm = parseAlgorithm(algorithmStr);
				createCourse(tenantId, new UpsertCourseRequest(code, name, description, algorithm));
			}
		}
	}

	// ─── Ratings ──────────────────────────────────────────────────────────────

	@Transactional(readOnly = true)
	public PageResponse<AdminRatingRow> getRatings(Long tenantId, Pageable pageable, String userEmail,
		String courseCode, String attributeName) {
		Long courseIdFilter = null;
		if (courseCode != null && !courseCode.isBlank()) {
			courseIdFilter = courseRepository.findByCodeAndTenantId(courseCode, tenantId)
				.map(Course::getId).orElse(-1L);
		}
		Long attributeIdFilter = null;
		if (attributeName != null && !attributeName.isBlank()) {
			attributeIdFilter = attributeRepository.findByValueAndTenantId(attributeName, tenantId)
				.map(Attribute::getId).orElse(-1L);
		}

		Page<UserCourseRating> page;
		if (userEmail != null && !userEmail.isBlank()) {
			String emailPattern = userEmail.toLowerCase();
			List<String> matchingUserIds = userRepository.findAll(
				(Specification<User>) (root, query, cb) -> cb.and(
					cb.equal(root.get("tenantId"), tenantId),
					cb.like(cb.lower(root.get("email")), "%" + emailPattern + "%")))
				.stream().map(User::getId).toList();
			if (matchingUserIds.isEmpty()) {
				return PageResponse.<AdminRatingRow>builder()
					.content(List.of()).totalElements(0).totalPages(0)
					.page(pageable.getPageNumber()).size(pageable.getPageSize()).build();
			}
			page = ratingRepository.findByTenantIdAndUserIdsAndFilters(tenantId, matchingUserIds,
				courseIdFilter, attributeIdFilter, pageable);
		} else {
			page = ratingRepository.findByTenantIdAndFilters(tenantId, null, courseIdFilter, attributeIdFilter,
				pageable);
		}

		Map<Long, String> courseIdToCode = courseRepository.findByTenantId(tenantId).stream()
			.collect(Collectors.toMap(Course::getId, Course::getCode));
		Map<Long, String> attrIdToName = attributeRepository.findByTenantId(tenantId).stream()
			.collect(Collectors.toMap(Attribute::getId, Attribute::getValue));
		List<String> userIds = page.getContent().stream().map(UserCourseRating::getUserId).distinct().toList();
		Map<String, String> userIdToEmail = userRepository.findByIdIn(userIds).stream()
			.collect(Collectors.toMap(User::getId, User::getEmail));

		return PageResponse.from(page.map(r -> new AdminRatingRow(
			r.getId(),
			userIdToEmail.getOrDefault(r.getUserId(), r.getUserId()),
			r.getCourseId(),
			courseIdToCode.getOrDefault(r.getCourseId(), String.valueOf(r.getCourseId())),
			r.getAttributeId(),
			attrIdToName.getOrDefault(r.getAttributeId(), String.valueOf(r.getAttributeId())),
			r.getScore(),
			r.getCreatedAt())));
	}

	@Transactional
	public void createRating(Long tenantId, CreateRatingRequest request) {
		courseRepository.findById(request.getCourseId())
			.filter(c -> tenantId.equals(c.getTenantId()))
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.NOT_FOUND));
		Attribute attr = attributeRepository.findByValueAndTenantId(request.getAttributeName(), tenantId)
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.NOT_FOUND));
		User user = userRepository.findByEmailAndTenantId(request.getUserEmail(), tenantId)
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.USER_NOT_FOUND));

		var existing = ratingRepository.findByUserIdAndCourseIdAndAttributeId(
			user.getId(), request.getCourseId(), attr.getId());
		if (existing.isPresent()) {
			UserCourseRating rating = existing.get();
			rating.setScore(request.getScore());
			ratingRepository.save(rating);
		} else {
			ratingRepository.save(UserCourseRating.builder()
				.userId(user.getId())
				.courseId(request.getCourseId())
				.attributeId(attr.getId())
				.score(request.getScore())
				.build());
		}
	}

	@Transactional
	public void updateRating(Long tenantId, Long ratingId, UpdateRatingRequest request) {
		UserCourseRating rating = ratingRepository.findById(ratingId)
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.NOT_FOUND));
		courseRepository.findById(rating.getCourseId())
			.filter(c -> tenantId.equals(c.getTenantId()))
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.NOT_FOUND));
		rating.setScore(request.getScore());
		ratingRepository.save(rating);
	}

	@Transactional
	public void deleteRating(Long tenantId, Long ratingId) {
		UserCourseRating rating = ratingRepository.findById(ratingId)
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.NOT_FOUND));
		courseRepository.findById(rating.getCourseId())
			.filter(c -> tenantId.equals(c.getTenantId()))
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.NOT_FOUND));
		ratingRepository.delete(rating);
	}

	@Transactional
	public void deleteRatings(Long tenantId, List<Long> ids) {
		ids.forEach(id -> deleteRating(tenantId, id));
	}

	@Transactional
	public void importRatings(Long tenantId, MultipartFile file) throws IOException {
		try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0);
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null)
					continue;
				String userEmail = getCellAsString(row.getCell(0));
				String courseCode = getCellAsString(row.getCell(1));
				String attributeName = getCellAsString(row.getCell(2));
				Integer score = getCellAsInteger(row.getCell(3));
				if (userEmail == null || courseCode == null || attributeName == null || score == null)
					continue;

				Course course = courseRepository.findByCodeAndTenantId(courseCode, tenantId)
					.orElseThrow(() -> new BadRequestException(GlobalErrorCode.NOT_FOUND));
				Attribute attr = attributeRepository.findByValueAndTenantId(attributeName, tenantId)
					.orElseThrow(() -> new BadRequestException(GlobalErrorCode.NOT_FOUND));
				User user = userRepository.findByEmailAndTenantId(userEmail, tenantId)
					.orElseThrow(() -> new BadRequestException(GlobalErrorCode.USER_NOT_FOUND));

				var existing = ratingRepository.findByUserIdAndCourseIdAndAttributeId(
					user.getId(), course.getId(), attr.getId());
				if (existing.isPresent()) {
					UserCourseRating rating = existing.get();
					rating.setScore(score);
					ratingRepository.save(rating);
				} else {
					ratingRepository.save(UserCourseRating.builder()
						.userId(user.getId())
						.courseId(course.getId())
						.attributeId(attr.getId())
						.score(score)
						.build());
				}
			}
		}
	}

	// ─── Helpers ──────────────────────────────────────────────────────────────

	private void scheduleRetrainAfterCommit(Long tenantId) {
		if (TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					retrainService.retrainAsync(tenantId);
				}
			});
		} else {
			retrainService.retrainAsync(tenantId);
		}
	}

	private String getCellAsString(Cell cell) {
		if (cell == null)
			return null;
		CellType type = cell.getCellType() == CellType.FORMULA
				? cell.getCachedFormulaResultType()
				: cell.getCellType();
		return switch (type) {
			case STRING -> cell.getStringCellValue().trim();
			case NUMERIC -> String.valueOf((long)cell.getNumericCellValue());
			case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
			default -> null;
		};
	}

	private Integer getCellAsInteger(Cell cell) {
		if (cell == null)
			return null;
		CellType type = cell.getCellType() == CellType.FORMULA
				? cell.getCachedFormulaResultType()
				: cell.getCellType();
		return switch (type) {
			case NUMERIC -> (int)cell.getNumericCellValue();
			case STRING -> {
				try {
					yield Integer.parseInt(cell.getStringCellValue().trim());
				} catch (NumberFormatException e) {
					yield null;
				}
			}
			default -> null;
		};
	}

	private Algorithm parseAlgorithm(String str) {
		if (str == null)
			return Algorithm.FS;
		try {
			return Algorithm.valueOf(str.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			return Algorithm.FS;
		}
	}

	private List<Role> parseRole(String str) {
		if (str == null)
			return List.of(Role.USER);
		try {
			return List.of(Role.valueOf(str.trim().toUpperCase()));
		} catch (IllegalArgumentException e) {
			return List.of(Role.USER);
		}
	}
}

package com.hcmus.course_recommendation.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
	Optional<User> findByEmail(String email);

	Optional<User> findByEmailAndTenantId(String email, Long tenantId);

	List<User> findByEmailInAndTenantId(List<String> emails, Long tenantId);

	List<User> findByIdIn(List<String> ids);

	List<User> findByTenantIdAndFullNameContainingIgnoreCase(Long tenantId, String fullName);

	List<User> findByTenantId(Long tenantId);
}

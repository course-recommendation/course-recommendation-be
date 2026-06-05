package com.hcmus.course_recommendation.recommendation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.recommendation.model.Attribute;

public interface AttributeRepository extends JpaRepository<Attribute, Long>, JpaSpecificationExecutor<Attribute> {
	List<Attribute> findByAlgorithmAndTenantId(Algorithm algorithm, Long tenantId);

	long countByTenantId(Long tenantId);

	long countByAlgorithmAndTenantId(Algorithm algorithm, Long tenantId);

	Optional<Attribute> findByValueAndTenantId(String value, Long tenantId);

	List<Attribute> findByTenantId(Long tenantId);
}

package com.hcmus.course_recommendation.recommendation.reposiroty;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.recommendation.model.Attribute;

public interface AttributeRepository extends JpaRepository<Attribute, Long> {
	List<Attribute> findByAlgorithmAndTenantId(Algorithm algorithm, Long tenantId);
}

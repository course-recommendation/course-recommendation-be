package com.hcmus.course_recommendation.common.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;

@Builder
public record PageResponse<T>(
	List<T> content,
	long totalElements,
	int totalPages,
	int page,
	int size
) {
	public static <T> PageResponse<T> from(Page<T> page) {
		return PageResponse.<T>builder()
			.content(page.getContent())
			.totalElements(page.getTotalElements())
			.totalPages(page.getTotalPages())
			.page(page.getNumber())
			.size(page.getSize())
			.build();
	}
}

package com.hcmus.course_recommendation.common;

import lombok.Builder;

@Builder
public record RestResponse<T>(boolean ok, T data) {
	public static <T> RestResponse<T> make(T data) {
		return RestResponse.<T>builder().ok(true).data(data).build();
	}

	public static <T> RestResponse<T> make() {
		return make(null);
	}

}

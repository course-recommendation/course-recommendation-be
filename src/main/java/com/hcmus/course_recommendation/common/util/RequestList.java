package com.hcmus.course_recommendation.common.util;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RequestList<T> {
	private boolean fetchAll = true;
	private List<T> data;

	public static <T> RequestList<T> defaultInstance() {
		return new RequestList<>();
	}
}

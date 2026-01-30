package com.hcmus.course_recommendation.course.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "algorithm"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = FSCourseExtraData.class, name = "FS"),
})
public interface CourseExtraData {

	@JsonIgnore
	CourseAlgorithm getAlgorithm();
}

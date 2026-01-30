package com.hcmus.course_recommendation.discuss.dto;

import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.discuss.model.Post;
import com.hcmus.course_recommendation.user.User;

import lombok.Builder;

@Builder
public record PostDetail(
	Post post,
	User user,
	Course course
) {
}

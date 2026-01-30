package com.hcmus.course_recommendation.auth.mapper;

import org.mapstruct.Mapper;

import com.hcmus.course_recommendation.auth.dto.RegisterRequest;
import com.hcmus.course_recommendation.user.User;

@Mapper
public interface AuthMapper {
	User toUser(RegisterRequest request);
}

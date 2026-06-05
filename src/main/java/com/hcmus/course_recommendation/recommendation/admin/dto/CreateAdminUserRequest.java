package com.hcmus.course_recommendation.recommendation.admin.dto;

import java.util.List;

import com.hcmus.course_recommendation.user.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdminUserRequest {
	private String email;
	private String password;
	private String fullName;
	private String avatarUrl;
	private List<Role> roles;
}

package com.hcmus.course_recommendation.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterAdminRequest {
	private String email;
	private String password;
	private long tenantId;
}

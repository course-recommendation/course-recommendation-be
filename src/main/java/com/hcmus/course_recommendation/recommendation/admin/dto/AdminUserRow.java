package com.hcmus.course_recommendation.recommendation.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.hcmus.course_recommendation.user.Role;

public record AdminUserRow(String id, String email, String fullName, List<Role> roles, LocalDateTime createdAt) {
}

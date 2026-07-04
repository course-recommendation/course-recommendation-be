package com.hcmus.course_recommendation.tenant.dto;

import lombok.Builder;

@Builder
public record TenantNicknameResponse(String nickname, boolean showNickname) {
}

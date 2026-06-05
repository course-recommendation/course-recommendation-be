package com.hcmus.course_recommendation.recommendation.admin.dto;

import java.util.List;

public record BulkDeleteRequest(List<String> ids) {
}

package com.hcmus.course_recommendation.auth;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "password_reset")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordReset {
	@Id
	@Builder.Default
	private String id = UUID.randomUUID().toString();
	private String userId;
	@Column(unique = true)
	private String token;
	private Instant expires;
	@Builder.Default
	private boolean used = false;
}

package com.hcmus.course_recommendation.user;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
@Builder(toBuilder = true)
public class User implements Serializable {
	@Id
	@Builder.Default
	private String id = UUID.randomUUID().toString();
	private String email;
	@JsonIgnore
	private String password;
	private String fullName;
	private String avatarUrl;
	private Long tenantId;
	@JsonIgnore
	@JdbcTypeCode(SqlTypes.JSON)
	private List<Role> roles;
}

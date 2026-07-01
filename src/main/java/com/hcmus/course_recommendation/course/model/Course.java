package com.hcmus.course_recommendation.course.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@Entity
public class Course implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String code;

	@Enumerated(EnumType.STRING)
	@JsonIgnore
	private Algorithm algorithm;

	@JsonIgnore
	private Long tenantId;

	private String name;
	private String description;
	private String thumbnailUrl;

	public String getThumbnailUrl() {
		return String.format("https://picsum.photos/seed/%s/1600/900", code);
	}
}

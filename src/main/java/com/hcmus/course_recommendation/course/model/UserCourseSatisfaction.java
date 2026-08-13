package com.hcmus.course_recommendation.course.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Overall satisfaction of a user with a course, as a whole 1-5 star.
 *
 * <p>Unlike {@link UserCourseRating}, whose attributes are descriptive bipolar axes where neither
 * pole is "better", this is a plain valenced rating and is therefore what belongs in TriRank's
 * user-item matrix R.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@Entity
public class UserCourseSatisfaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String userId;
	private Long courseId;
	private Integer score;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
}

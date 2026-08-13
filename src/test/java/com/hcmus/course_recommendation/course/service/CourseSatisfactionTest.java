package com.hcmus.course_recommendation.course.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.hcmus.course_recommendation.common.exception.NotFoundException;
import com.hcmus.course_recommendation.course.model.UserCourseSatisfaction;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.course.repository.FsCourseSentimentRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseRatingRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseSatisfactionRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseStatusRepository;

/**
 * Covers the overall satisfaction write path, which is what feeds TriRank's user-item matrix R. The
 * attribute ratings deliberately do not: they sit on bipolar axes where neither pole is better.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CourseSatisfactionTest {

	private static final String USER_ID = "USER_A";
	private static final long TENANT_ID = 2L;
	private static final long COURSE_ID = 7L;

	@Mock
	private UserCourseStatusRepository userCourseStatusRepository;
	@Mock
	private CourseRepository courseRepository;
	@Mock
	private UserCourseRatingRepository userCourseRatingRepository;
	@Mock
	private UserCourseSatisfactionRepository userCourseSatisfactionRepository;
	@Mock
	private FsCourseSentimentRepository fsCourseSentimentRepository;

	private CourseService courseService;

	@BeforeEach
	void setUp() {
		courseService = new CourseService(userCourseStatusRepository, courseRepository, userCourseRatingRepository,
			userCourseSatisfactionRepository, fsCourseSentimentRepository);
		when(courseRepository.existsByIdAndTenantId(COURSE_ID, TENANT_ID)).thenReturn(true);
		when(userCourseSatisfactionRepository.findByUserIdAndCourseId(anyString(), anyLong()))
			.thenReturn(Optional.empty());
	}

	private UserCourseSatisfaction savedSatisfaction() {
		var captor = ArgumentCaptor.forClass(UserCourseSatisfaction.class);
		verify(userCourseSatisfactionRepository).save(captor.capture());
		return captor.getValue();
	}

	@Test
	void shouldInsertARowWhenTheUserHasNotRatedTheCourseYet() {
		courseService.rateCourseSatisfaction(USER_ID, TENANT_ID, COURSE_ID, 4);

		var saved = savedSatisfaction();
		assertAll(
			() -> assertNull(saved.getId()),
			() -> assertEquals(USER_ID, saved.getUserId()),
			() -> assertEquals(COURSE_ID, saved.getCourseId()),
			() -> assertEquals(4, saved.getScore()));
	}

	/** Updating has to reuse the row's id, or the unique (user, course) key would reject the insert. */
	@Test
	void shouldOverwriteTheExistingRowInPlace() {
		when(userCourseSatisfactionRepository.findByUserIdAndCourseId(USER_ID, COURSE_ID))
			.thenReturn(Optional.of(UserCourseSatisfaction.builder()
				.id(99L)
				.userId(USER_ID)
				.courseId(COURSE_ID)
				.score(2)
				.build()));

		courseService.rateCourseSatisfaction(USER_ID, TENANT_ID, COURSE_ID, 5);

		var saved = savedSatisfaction();
		assertAll(
			() -> assertEquals(99L, saved.getId()),
			() -> assertEquals(5, saved.getScore()));
	}

	/**
	 * The star widget reports 0 when the user clears their choice. Persisting a 0 would put a value off
	 * the 1-5 scale into R, so the row is removed instead.
	 */
	@Test
	void shouldDeleteTheRowWhenTheRatingIsCleared() {
		when(userCourseSatisfactionRepository.findByUserIdAndCourseId(USER_ID, COURSE_ID))
			.thenReturn(Optional.of(UserCourseSatisfaction.builder().id(99L).build()));

		courseService.rateCourseSatisfaction(USER_ID, TENANT_ID, COURSE_ID, 0);

		verify(userCourseSatisfactionRepository).deleteById(99L);
		verify(userCourseSatisfactionRepository, never()).save(any());
	}

	@Test
	void shouldDoNothingWhenClearingARatingThatWasNeverGiven() {
		courseService.rateCourseSatisfaction(USER_ID, TENANT_ID, COURSE_ID, null);

		verify(userCourseSatisfactionRepository, never()).deleteById(anyLong());
		verify(userCourseSatisfactionRepository, never()).save(any());
	}

	@Test
	void shouldClampScoresOutsideTheStarScale() {
		courseService.rateCourseSatisfaction(USER_ID, TENANT_ID, COURSE_ID, 9);

		assertEquals(5, savedSatisfaction().getScore());
	}

	@Test
	void shouldRejectACourseFromAnotherTenant() {
		when(courseRepository.existsByIdAndTenantId(COURSE_ID, TENANT_ID)).thenReturn(false);

		assertThrows(NotFoundException.class,
			() -> courseService.rateCourseSatisfaction(USER_ID, TENANT_ID, COURSE_ID, 4));
		verify(userCourseSatisfactionRepository, never()).save(any());
	}

	/** Guards rows written outside the service, e.g. by a direct SQL fix-up. */
	@Test
	void toStarScoreShouldConstrainToTheStarScale() {
		assertAll(
			() -> assertNull(CourseService.toStarScore(null)),
			() -> assertEquals(3, CourseService.toStarScore(3)),
			() -> assertEquals(1, CourseService.toStarScore(0)),
			() -> assertEquals(1, CourseService.toStarScore(-2)),
			() -> assertEquals(5, CourseService.toStarScore(7)));
	}
}

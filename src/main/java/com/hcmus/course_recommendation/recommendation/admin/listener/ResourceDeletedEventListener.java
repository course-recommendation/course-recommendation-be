package com.hcmus.course_recommendation.recommendation.admin.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.hcmus.course_recommendation.common.event.DeletedResourceApplicationEvent;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.repository.FsCourseSentimentRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseRatingRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseStatusRepository;
import com.hcmus.course_recommendation.recommendation.admin.RetrainService;
import com.hcmus.course_recommendation.recommendation.model.Attribute;
import com.hcmus.course_recommendation.user.User;
import com.hcmus.course_recommendation.recommendation.fs.repository.RecommendationResultRepository;
import com.hcmus.course_recommendation.recommendation.fs.repository.UserPreferenceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceDeletedEventListener {

	private final UserCourseRatingRepository ratingRepository;
	private final UserCourseStatusRepository statusRepository;
	private final UserPreferenceRepository preferenceRepository;
	private final RecommendationResultRepository recommendationResultRepository;
	private final FsCourseSentimentRepository fsCourseSentimentRepository;
	private final RetrainService retrainService;

	@EventListener
	@Transactional
	public void onAttributeDeleted(DeletedResourceApplicationEvent<Attribute> event) {
		Attribute attr = event.getData();
		ratingRepository.deleteByAttributeId(attr.getId());
		scheduleRetrainAfterCommit(attr.getTenantId());
	}

	@EventListener
	@Transactional
	public void onCourseDeleted(DeletedResourceApplicationEvent<Course> event) {
		Course course = event.getData();
		ratingRepository.deleteByCourseId(course.getId());
		statusRepository.deleteByCourseId(course.getId());
		fsCourseSentimentRepository.findByCourseId(course.getId()).ifPresent(fsCourseSentimentRepository::delete);
		scheduleRetrainAfterCommit(course.getTenantId());
	}

	@EventListener
	@Transactional
	public void onUserDeleted(DeletedResourceApplicationEvent<User> event) {
		User user = event.getData();
		ratingRepository.deleteByUserId(user.getId());
		statusRepository.deleteByUserId(user.getId());
		preferenceRepository.deleteByUserId(user.getId());
		recommendationResultRepository.deleteByUserId(user.getId());
	}

	private void scheduleRetrainAfterCommit(Long tenantId) {
		if (TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					retrainService.retrainAsync(tenantId);
				}
			});
		} else {
			retrainService.retrainAsync(tenantId);
		}
	}
}

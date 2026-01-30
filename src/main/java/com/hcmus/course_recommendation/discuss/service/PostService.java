package com.hcmus.course_recommendation.discuss.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.course.service.CourseService;
import com.hcmus.course_recommendation.discuss.dto.CreatePostCommentRequest;
import com.hcmus.course_recommendation.discuss.dto.CreatePostRequest;
import com.hcmus.course_recommendation.discuss.dto.FindPostDetailsRequest;
import com.hcmus.course_recommendation.discuss.dto.PostCommentDetail;
import com.hcmus.course_recommendation.discuss.dto.PostDetail;
import com.hcmus.course_recommendation.discuss.model.Post;
import com.hcmus.course_recommendation.discuss.model.PostComment;
import com.hcmus.course_recommendation.discuss.repository.PostCommentRepository;
import com.hcmus.course_recommendation.discuss.repository.PostRepository;
import com.hcmus.course_recommendation.user.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostService {
	private final PostRepository postRepository;
	private final UserService userService;
	private final PostCommentRepository postCommentRepository;
	private final CourseService courseService;
	private final CourseRepository courseRepository;

	@Transactional
	public void createPost(CreatePostRequest request) {
		postRepository.save(Post.builder()
			.content(request.getContent())
			.userId(request.getUserId())
			.courseId(request.getCourseId())
			.build());
	}

	@Transactional(readOnly = true)
	public List<PostDetail> findPostDetails(FindPostDetailsRequest request, Sort sort) {
		var courses = courseRepository.findByAlgorithmAndDataset(request.getCourseDomain().getAlgorithm(),
			request.getCourseDomain().getDataset());
		var courseIds = courses.stream().map(Course::getId).toList();

		var posts = postRepository.findByCourseIdIn(courseIds, sort);
		return toPostDetails(posts);
	}

	@Transactional(readOnly = true)
	public List<PostDetail> toPostDetails(List<Post> posts) {
		var userIds = posts.stream().map(Post::getUserId).toList();
		var userIdToUser = userService.getUserIdToUserMapByUserIds(userIds);

		var courseIds = posts.stream().map(Post::getCourseId).toList();
		var courseIdToCourse = courseService.getCourseIdToCourseMapByCourseIds(courseIds);

		return posts.stream()
			.map(post -> PostDetail.builder()
				.post(post)
				.user(userIdToUser.get(post.getUserId()))
				.course(courseIdToCourse.get(post.getCourseId()))
				.build())
			.toList();
	}

	@Transactional
	public void createPostComment(CreatePostCommentRequest request) {
		postCommentRepository.save(PostComment.builder()
			.postId(request.getPostId())
			.content(request.getContent())
			.userId(request.getUserId())
			.build());
	}

	@Transactional(readOnly = true)
	public List<PostCommentDetail> findPostCommentsByPostId(Long postId) {
		var postComments = postCommentRepository.findByPostId(postId);
		var userIds = postComments.stream().map(PostComment::getUserId).toList();
		var userIdToUser = userService.getUserIdToUserMapByUserIds(userIds);
		return postComments.stream()
			.map(postComment -> PostCommentDetail.builder()
				.postComment(postComment)
				.user(userIdToUser.get(postComment.getUserId()))
				.build())
			.toList();
	}
}

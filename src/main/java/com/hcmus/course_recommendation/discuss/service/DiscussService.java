package com.hcmus.course_recommendation.discuss.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.course_recommendation.common.dto.PageResponse;
import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.course.service.CourseService;
import com.hcmus.course_recommendation.discuss.dto.CreatePostCommentRequest;
import com.hcmus.course_recommendation.discuss.dto.CreatePostRequest;
import com.hcmus.course_recommendation.discuss.dto.FindPostDetailsRequest;
import com.hcmus.course_recommendation.discuss.dto.PostCommentDetail;
import com.hcmus.course_recommendation.discuss.dto.PostDetail;
import com.hcmus.course_recommendation.discuss.dto.VoteRequest;
import com.hcmus.course_recommendation.discuss.model.Post;
import com.hcmus.course_recommendation.discuss.model.PostComment;
import com.hcmus.course_recommendation.discuss.model.PostCommentVote;
import com.hcmus.course_recommendation.discuss.model.PostVote;
import com.hcmus.course_recommendation.discuss.model.VoteType;
import com.hcmus.course_recommendation.discuss.repository.PostCommentRepository;
import com.hcmus.course_recommendation.discuss.repository.PostCommentVoteRepository;
import com.hcmus.course_recommendation.discuss.repository.PostRepository;
import com.hcmus.course_recommendation.discuss.repository.PostVoteRepository;
import com.hcmus.course_recommendation.user.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiscussService {
	private final PostRepository postRepository;
	private final UserService userService;
	private final PostCommentRepository postCommentRepository;
	private final CourseService courseService;
	private final CourseRepository courseRepository;
	private final PostVoteRepository postVoteRepository;
	private final PostCommentVoteRepository postCommentVoteRepository;

	@Transactional
	public void createPost(CreatePostRequest request) {
		postRepository.save(Post.builder()
			.content(request.getContent())
			.userId(request.getUserId())
			.courseCode(request.getCourseCode())
			.algorithm(request.getAlgorithm())
			.tenantId(request.getTenantId())
			.build());
	}

	@Transactional(readOnly = true)
	public PageResponse<PostDetail> findPostDetails(FindPostDetailsRequest request, Pageable pageable,
		String userId) {
		List<Course> courses;
		if (request.getCourseIdsRequest().isFetchAll()) {
			courses = courseRepository.findByAlgorithmAndTenantId(request.getAlgorithm(), request.getTenantId());
		} else {
			courses = courseRepository.findByAlgorithmAndTenantIdAndCodeIn(request.getAlgorithm(),
				request.getTenantId(), request.getCourseIdsRequest().getData());
		}

		var courseIds = courses.stream().map(Course::getCode).toList();

		var authorIdsRequest = request.getAuthorIdsRequest();
		var filterByAuthor = authorIdsRequest != null
			&& !authorIdsRequest.isFetchAll()
			&& authorIdsRequest.getData() != null
			&& !authorIdsRequest.getData().isEmpty();

		// Many posts share the same createdAt instant, so sorting on it alone is not a total
		// order: paging over it non-deterministically duplicates/skips rows. Append id as a
		// tiebreaker to guarantee a stable order across pages.
		var stableSort = pageable.getSort().and(Sort.by(Sort.Direction.DESC, "id"));
		pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), stableSort);

		Page<Post> postsPage = filterByAuthor
			? postRepository.findByAlgorithmAndTenantIdAndCourseCodeInAndUserIdIn(
			request.getAlgorithm(), request.getTenantId(), courseIds, authorIdsRequest.getData(), pageable)
			: postRepository.findByAlgorithmAndTenantIdAndCourseCodeIn(
			request.getAlgorithm(), request.getTenantId(), courseIds, pageable);

		var postDetails = toPostDetails(request.getAlgorithm(), request.getTenantId(), postsPage.getContent(),
			userId);

		return PageResponse.<PostDetail>builder()
			.content(postDetails)
			.totalElements(postsPage.getTotalElements())
			.totalPages(postsPage.getTotalPages())
			.page(postsPage.getNumber())
			.size(postsPage.getSize())
			.build();
	}

	@Transactional(readOnly = true)
	public List<PostDetail> toPostDetails(Algorithm algorithm, Long tenantId, List<Post> posts, String userId) {
		var userIds = posts.stream().map(Post::getUserId).toList();
		var userIdToUser = userService.getUserIdToUserMapByUserIds(userIds);

		var courseIds = posts.stream().map(Post::getCourseCode).toList();
		var courseIdToCourse = courseService.getCourseIdToCourseMapByCourseIds(algorithm, tenantId, courseIds);

		var postIds = posts.stream().map(Post::getId).toList();
		var votes = postVoteRepository.findByPostIdIn(postIds);

		Map<Long, Integer> voteCountByPostId = votes.stream()
			.collect(Collectors.groupingBy(PostVote::getPostId,
				Collectors.summingInt(v -> v.getVoteType() == VoteType.UPVOTE ? 1 : -1)));

		Map<Long, VoteType> userVoteByPostId = votes.stream()
			.filter(v -> v.getUserId().equals(userId))
			.collect(Collectors.toMap(PostVote::getPostId, PostVote::getVoteType));

		return posts.stream()
			.map(post -> PostDetail.builder()
				.post(post)
				.user(userIdToUser.get(post.getUserId()))
				.course(courseIdToCourse.get(post.getCourseCode()))
				.voteCount(voteCountByPostId.getOrDefault(post.getId(), 0))
				.userVote(userVoteByPostId.get(post.getId()))
				.build())
			.toList();
	}

	@Transactional
	public void createPostComment(CreatePostCommentRequest request) {
		postCommentRepository.save(PostComment.builder()
			.postId(request.getPostId())
			.parentCommentId(request.getParentCommentId())
			.content(request.getContent())
			.userId(request.getUserId())
			.build());
	}

	@Transactional(readOnly = true)
	public List<PostCommentDetail> findPostCommentsByPostId(Long postId, String userId) {
		var allComments = postCommentRepository.findByPostIdOrderByIdDesc(postId);

		var commentIds = allComments.stream().map(PostComment::getId).toList();
		var votes = postCommentVoteRepository.findByCommentIdIn(commentIds);

		Map<Long, Integer> voteCountByCommentId = votes.stream()
			.collect(Collectors.groupingBy(PostCommentVote::getCommentId,
				Collectors.summingInt(v -> v.getVoteType() == VoteType.UPVOTE ? 1 : -1)));

		Map<Long, VoteType> userVoteByCommentId = votes.stream()
			.filter(v -> v.getUserId().equals(userId))
			.collect(Collectors.toMap(PostCommentVote::getCommentId, PostCommentVote::getVoteType));

		var userIds = allComments.stream().map(PostComment::getUserId).toList();
		var userIdToUser = userService.getUserIdToUserMapByUserIds(userIds);

		Map<Long, List<PostComment>> repliesByParentId = allComments.stream()
			.filter(c -> c.getParentCommentId() != null)
			.collect(Collectors.groupingBy(PostComment::getParentCommentId));

		return allComments.stream()
			.filter(c -> c.getParentCommentId() == null)
			.map(comment -> {
				var nestedReplies = repliesByParentId.getOrDefault(comment.getId(), List.of()).stream()
					.map(reply -> PostCommentDetail.builder()
						.postComment(reply)
						.user(userIdToUser.get(reply.getUserId()))
						.voteCount(voteCountByCommentId.getOrDefault(reply.getId(), 0))
						.userVote(userVoteByCommentId.get(reply.getId()))
						.replies(List.of())
						.build())
					.toList();

				return PostCommentDetail.builder()
					.postComment(comment)
					.user(userIdToUser.get(comment.getUserId()))
					.voteCount(voteCountByCommentId.getOrDefault(comment.getId(), 0))
					.userVote(userVoteByCommentId.get(comment.getId()))
					.replies(nestedReplies)
					.build();
			})
			.toList();
	}

	@Transactional
	public void votePost(Long postId, String userId, VoteRequest request) {
		var existing = postVoteRepository.findByPostIdAndUserId(postId, userId);
		if (existing.isPresent()) {
			existing.get().setVoteType(request.getVoteType());
			postVoteRepository.save(existing.get());
		} else {
			postVoteRepository.save(PostVote.builder()
				.postId(postId)
				.userId(userId)
				.voteType(request.getVoteType())
				.build());
		}
	}

	@Transactional
	public void removePostVote(Long postId, String userId) {
		postVoteRepository.deleteByPostIdAndUserId(postId, userId);
	}

	@Transactional
	public void votePostComment(Long commentId, String userId, VoteRequest request) {
		var existing = postCommentVoteRepository.findByCommentIdAndUserId(commentId, userId);
		if (existing.isPresent()) {
			existing.get().setVoteType(request.getVoteType());
			postCommentVoteRepository.save(existing.get());
		} else {
			postCommentVoteRepository.save(PostCommentVote.builder()
				.commentId(commentId)
				.userId(userId)
				.voteType(request.getVoteType())
				.build());
		}
	}

	@Transactional
	public void removePostCommentVote(Long commentId, String userId) {
		postCommentVoteRepository.deleteByCommentIdAndUserId(commentId, userId);
	}
}

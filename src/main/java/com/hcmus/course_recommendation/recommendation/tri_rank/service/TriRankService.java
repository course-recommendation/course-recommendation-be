package com.hcmus.course_recommendation.recommendation.tri_rank.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.hcmus.course_recommendation.course.model.Algorithm;
import com.hcmus.course_recommendation.course.model.Course;
import com.hcmus.course_recommendation.course.model.UserCourseRating;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseRatingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriRankService {

	static final Algorithm TRI_RANK_ALGORITHM = Algorithm.TRI_RANK;
	static final String STORAGE_ACCOUNT_NAME = "stcourserecom";
	static final String CONTAINER_NAME = "dataset";
	static final String RATING_FILE_NAME = "rating.txt";
	static final String SENTIMENT_FILE_NAME = "sentiment.txt";
	static final long FIXED_TIMESTAMP = 1400630400L;
	static final int SENTIMENT_GOOD_SCORE_THRESHOLD = 3;

	private final CourseRepository courseRepository;
	private final UserCourseRatingRepository userCourseRatingRepository;

	public void exportTriRankDatasetToAzure(Long tenantId) {
		var courseIdToCourseCode = courseRepository.findByAlgorithmAndTenantId(TRI_RANK_ALGORITHM, tenantId).stream()
			.collect(Collectors.toMap(Course::getId, Course::getCode));

		var userCourseRatings = userCourseRatingRepository.findByAlgorithmAndTenantId(TRI_RANK_ALGORITHM, tenantId)
			.stream()
			.filter(rating -> courseIdToCourseCode.containsKey(rating.getCourseId()))
			.toList();

		var ratingContent = buildRatingFileContent(userCourseRatings, courseIdToCourseCode);
		var sentimentContent = buildSentimentFileContent(userCourseRatings, courseIdToCourseCode);

		var tempDirectory = createTempDirectory();
		try {
			var ratingFile = tempDirectory.resolve(RATING_FILE_NAME);
			var sentimentFile = tempDirectory.resolve(SENTIMENT_FILE_NAME);

			Files.writeString(ratingFile, ratingContent, StandardCharsets.UTF_8);
			Files.writeString(sentimentFile, sentimentContent, StandardCharsets.UTF_8);

			var containerClient = getContainerClient();
			uploadFile(containerClient, ratingFile, RATING_FILE_NAME);
			uploadFile(containerClient, sentimentFile, SENTIMENT_FILE_NAME);

			log.info("Uploaded TriRank dataset files to Azure Blob Storage container '{}' in account '{}'",
				CONTAINER_NAME, STORAGE_ACCOUNT_NAME);
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to generate TriRank dataset files", exception);
		} finally {
			deleteRecursively(tempDirectory);
		}
	}

	String buildRatingFileContent(List<UserCourseRating> userCourseRatings, Map<Long, String> courseIdToCourseCode) {
		return userCourseRatings.stream()
			.filter(rating -> courseIdToCourseCode.containsKey(rating.getCourseId()))
			.sorted(Comparator.comparing(UserCourseRating::getUserId)
				.thenComparing(rating -> courseIdToCourseCode.get(rating.getCourseId()))
				.thenComparing(UserCourseRating::getAttributeValue))
			.collect(Collectors.groupingBy(
				rating -> new UserCourseKey(rating.getUserId(), courseIdToCourseCode.get(rating.getCourseId())),
				LinkedHashMap::new,
				Collectors.toList()))
			.entrySet().stream()
			.map(entry -> entry.getKey().userId() + "," + entry.getKey().courseCode() + ","
				+ formatAverageScore(entry.getValue()) + "," + FIXED_TIMESTAMP)
			.collect(Collectors.joining("\n"));
	}

	String buildSentimentFileContent(List<UserCourseRating> userCourseRatings, Map<Long, String> courseIdToCourseCode) {
		return userCourseRatings.stream()
			.filter(rating -> courseIdToCourseCode.containsKey(rating.getCourseId()))
			.filter(rating -> Objects.nonNull(rating.getScore()) && rating.getScore() >= SENTIMENT_GOOD_SCORE_THRESHOLD)
			.sorted(Comparator.comparing(UserCourseRating::getUserId)
				.thenComparing(rating -> courseIdToCourseCode.get(rating.getCourseId()))
				.thenComparing(UserCourseRating::getAttributeValue))
			.collect(Collectors.groupingBy(
				rating -> new UserCourseKey(rating.getUserId(), courseIdToCourseCode.get(rating.getCourseId())),
				LinkedHashMap::new,
				Collectors.toList()))
			.entrySet().stream()
			.map(entry -> {
				var attributes = entry.getValue().stream()
					.map(rating -> rating.getAttributeValue() + ":good:1")
					.collect(Collectors.joining(","));
				return entry.getKey().userId() + "," + entry.getKey().courseCode() + "," + attributes;
			})
			.collect(Collectors.joining("\n"));
	}

	private BlobContainerClient getContainerClient() {
		BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
			.endpoint("https://" + STORAGE_ACCOUNT_NAME + ".blob.core.windows.net")
			.credential(new DefaultAzureCredentialBuilder().build())
			.buildClient();

		var containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
		containerClient.createIfNotExists();
		return containerClient;
	}

	private void uploadFile(BlobContainerClient containerClient, Path filePath, String blobName) {
		containerClient.getBlobClient(blobName).uploadFromFile(filePath.toString(), true);
	}

	private Path createTempDirectory() {
		try {
			return Files.createTempDirectory("trirank-export-");
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to create temporary export directory", exception);
		}
	}

	private void deleteRecursively(Path directory) {
		if (directory == null || Files.notExists(directory)) {
			return;
		}

		try (var paths = Files.walk(directory)) {
			paths.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException exception) {
					throw new IllegalStateException("Failed to clean up temporary export files", exception);
				}
			});
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to clean up temporary export files", exception);
		}
	}

	private String formatAverageScore(List<UserCourseRating> userCourseRatings) {
		var sum = userCourseRatings.stream()
			.map(UserCourseRating::getScore)
			.filter(Objects::nonNull)
			.map(BigDecimal::valueOf)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		return sum.divide(
				BigDecimal.valueOf(userCourseRatings.size()),
				6,
				RoundingMode.HALF_UP
			)
			.toPlainString();
	}

	private record UserCourseKey(String userId, String courseCode) {
	}
}

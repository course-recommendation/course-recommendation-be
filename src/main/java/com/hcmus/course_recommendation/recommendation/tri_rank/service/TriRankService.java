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
import com.hcmus.course_recommendation.course.model.UserCourseSatisfaction;
import com.hcmus.course_recommendation.course.repository.CourseRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseRatingRepository;
import com.hcmus.course_recommendation.course.repository.UserCourseSatisfactionRepository;
import com.hcmus.course_recommendation.recommendation.model.Attribute;
import com.hcmus.course_recommendation.recommendation.repository.AttributeRepository;
import com.hcmus.course_recommendation.recommendation.tri_rank.TriRankAspects;
import com.hcmus.course_recommendation.recommendation.tri_rank.client.TriRankClient;
import com.hcmus.course_recommendation.recommendation.tri_rank.client.dto.ClientTriRankTrainRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriRankService {

	static final Algorithm TRI_RANK_ALGORITHM = Algorithm.TRI_RANK;
	static final String STORAGE_ACCOUNT_NAME = "stcourserecom";
	static final String CONTAINER_NAME = "dataset";
	static final String RATING_FILE_SUFFIX = ".rating.txt";
	static final String SENTIMENT_FILE_SUFFIX = ".sentiment.txt";
	static final long FIXED_TIMESTAMP = 1400630400L;
	/**
	 * TriRank ignores sentiment polarity entirely (it only reads which aspects a review mentions), so
	 * the opinion word and polarity are placeholders. The pole is already carried by the aspect name.
	 */
	static final String SENTIMENT_TUPLE_SUFFIX = ":mentioned:1";

	private final CourseRepository courseRepository;
	private final UserCourseRatingRepository userCourseRatingRepository;
	private final UserCourseSatisfactionRepository userCourseSatisfactionRepository;
	private final AttributeRepository attributeRepository;
	private final TriRankClient triRankClient;

	public void trainTriRank(Long tenantId) {
		exportTriRankDatasetToAzure(tenantId);
		triRankClient.train(ClientTriRankTrainRequest.builder().tenantId(tenantId).build());
	}

	public void exportTriRankDatasetToAzure(Long tenantId) {
		var courseIdToCourseCode = courseRepository.findByAlgorithmAndTenantId(TRI_RANK_ALGORITHM, tenantId).stream()
			.collect(Collectors.toMap(Course::getId, Course::getCode));

		var attributeIdToValue = attributeRepository.findByAlgorithmAndTenantId(TRI_RANK_ALGORITHM, tenantId).stream()
			.collect(Collectors.toMap(Attribute::getId, Attribute::getValue));

		var userCourseRatings = userCourseRatingRepository.findByAlgorithmAndTenantId(TRI_RANK_ALGORITHM, tenantId)
			.stream()
			.filter(rating -> courseIdToCourseCode.containsKey(rating.getCourseId()))
			.toList();

		var satisfactionByUserCourse = userCourseSatisfactionRepository
			.findByAlgorithmAndTenantId(TRI_RANK_ALGORITHM, tenantId)
			.stream()
			.filter(satisfaction -> courseIdToCourseCode.containsKey(satisfaction.getCourseId()))
			.filter(satisfaction -> Objects.nonNull(satisfaction.getScore()))
			.collect(Collectors.toMap(
				satisfaction -> new UserCourseKey(satisfaction.getUserId(),
					courseIdToCourseCode.get(satisfaction.getCourseId())),
				UserCourseSatisfaction::getScore,
				(existing, ignored) -> existing));

		var ratingContent = buildRatingFileContent(userCourseRatings, courseIdToCourseCode, satisfactionByUserCourse);
		var sentimentContent = buildSentimentFileContent(userCourseRatings, courseIdToCourseCode, attributeIdToValue);

		var ratingBlobName = tenantId + RATING_FILE_SUFFIX;
		var sentimentBlobName = tenantId + SENTIMENT_FILE_SUFFIX;
		var tempDirectory = createTempDirectory();
		try {
			var ratingFile = tempDirectory.resolve(ratingBlobName);
			var sentimentFile = tempDirectory.resolve(sentimentBlobName);

			Files.writeString(ratingFile, ratingContent, StandardCharsets.UTF_8);
			Files.writeString(sentimentFile, sentimentContent, StandardCharsets.UTF_8);

			var containerClient = getContainerClient();
			uploadFile(containerClient, ratingFile, ratingBlobName);
			uploadFile(containerClient, sentimentFile, sentimentBlobName);

			log.info("Uploaded TriRank dataset files to Azure Blob Storage container '{}' in account '{}'",
				CONTAINER_NAME, STORAGE_ACCOUNT_NAME);
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to generate TriRank dataset files", exception);
		} finally {
			deleteRecursively(tempDirectory);
		}
	}

	/**
	 * Builds the user-item matrix R from the overall satisfaction rating.
	 *
	 * <p>R is meant to express how much a user liked an item. The attribute scores cannot express
	 * that: they sit on bipolar descriptive axes where neither end is better, and averaging seven of
	 * them is pinned near the midpoint by the central limit theorem - on the generated dataset that
	 * average had SD 0.49 with 67% of all values inside [2.5, 3.5], so R carried almost no signal.
	 *
	 * <p>Rows without a satisfaction score fall back to the old average so that datasets predating
	 * {@code user_course_satisfaction} still export.
	 */
	String buildRatingFileContent(List<UserCourseRating> userCourseRatings, Map<Long, String> courseIdToCourseCode,
		Map<UserCourseKey, Double> satisfactionByUserCourse) {
		return groupByUserCourse(userCourseRatings, courseIdToCourseCode).entrySet().stream()
			.map(entry -> {
				var satisfaction = satisfactionByUserCourse.get(entry.getKey());
				var score = satisfaction != null
					? BigDecimal.valueOf(satisfaction).setScale(6, RoundingMode.HALF_UP).toPlainString()
					: formatAverageScore(entry.getValue());
				return entry.getKey().userId() + "," + entry.getKey().courseCode() + "," + score + ","
					+ FIXED_TIMESTAMP;
			})
			.collect(Collectors.joining("\n"));
	}

	/**
	 * Builds the sentiment file, emitting one aspect per <em>pole</em> of each bipolar attribute.
	 *
	 * <p>The previous version kept only scores at or above 3 and wrote them all as the same
	 * {@code <attribute>:good:1} token. That threw the score away twice over: every surviving value
	 * collapsed to a constant, so the item-aspect matrix degenerated into a mention count (the score
	 * the UI showed correlated with mention rate at r=0.91), and scores of 1-2 produced no edge at
	 * all, leaving the entire low half of every axis invisible to the model.
	 *
	 * <p>Now a rating at the low end of its axis contributes to {@code <attribute>@low} and one at the
	 * high end to {@code <attribute>@high}, so both poles are representable. See
	 * {@link com.hcmus.course_recommendation.recommendation.tri_rank.TriRankAspects}.
	 */
	String buildSentimentFileContent(List<UserCourseRating> userCourseRatings, Map<Long, String> courseIdToCourseCode,
		Map<Long, String> attributeIdToValue) {
		var cutoffByAttributeId = computePoleCutoffs(userCourseRatings);

		return groupByUserCourse(userCourseRatings, courseIdToCourseCode).entrySet().stream()
			.map(entry -> {
				var aspects = entry.getValue().stream()
					.map(rating -> toPoleAspect(rating, attributeIdToValue, cutoffByAttributeId))
					.filter(Objects::nonNull)
					.map(aspect -> aspect + SENTIMENT_TUPLE_SUFFIX)
					.collect(Collectors.joining(","));
				return aspects.isEmpty()
					? null
					: entry.getKey().userId() + "," + entry.getKey().courseCode() + "," + aspects;
			})
			.filter(Objects::nonNull)
			.collect(Collectors.joining("\n"));
	}

	private String toPoleAspect(UserCourseRating rating, Map<Long, String> attributeIdToValue,
		Map<Long, PoleCutoff> cutoffByAttributeId) {
		if (rating.getScore() == null) {
			return null;
		}
		var cutoff = cutoffByAttributeId.get(rating.getAttributeId());
		if (cutoff == null) {
			return null;
		}
		var attributeValue = attributeIdToValue.getOrDefault(rating.getAttributeId(),
			String.valueOf(rating.getAttributeId()));
		if (rating.getScore() <= cutoff.lowCut()) {
			return TriRankAspects.lowPoleAspect(attributeValue);
		}
		if (rating.getScore() >= cutoff.highCut()) {
			return TriRankAspects.highPoleAspect(attributeValue);
		}
		// Genuinely in the middle of the axis: it leans neither way, so it should not pull a course
		// towards either pole. Such ratings still show up in the explanation, which uses the mean.
		return null;
	}

	/**
	 * Picks, per attribute, the score at or below which a rating counts as the low pole and the score
	 * at or above which it counts as the high pole, using that attribute's own terciles.
	 *
	 * <p>Terciles rather than a hard-coded 2 and 4 because the observed spread is a property of the
	 * data, not of the problem: the same split measured 38/24/38 on one generated dataset and
	 * 37/30/34 on another. Deriving the cut keeps the two poles balanced whatever the distribution.
	 */
	private Map<Long, PoleCutoff> computePoleCutoffs(List<UserCourseRating> userCourseRatings) {
		Map<Long, int[]> histogramByAttributeId = new LinkedHashMap<>();
		for (var rating : userCourseRatings) {
			if (rating.getScore() == null) {
				continue;
			}
			var score = Math.clamp(rating.getScore().longValue(), TriRankAspects.MIN_SCORE, TriRankAspects.MAX_SCORE);
			histogramByAttributeId
				.computeIfAbsent(rating.getAttributeId(), attributeId -> new int[TriRankAspects.MAX_SCORE + 1])
				[score]++;
		}

		Map<Long, PoleCutoff> cutoffByAttributeId = new LinkedHashMap<>();
		histogramByAttributeId.forEach((attributeId, histogram) -> {
			var total = 0;
			for (var score = TriRankAspects.MIN_SCORE; score <= TriRankAspects.MAX_SCORE; score++) {
				total += histogram[score];
			}
			if (total == 0) {
				return;
			}

			var lowCut = TriRankAspects.MIN_SCORE;
			var cumulative = 0;
			for (var score = TriRankAspects.MIN_SCORE; score <= TriRankAspects.MAX_SCORE; score++) {
				cumulative += histogram[score];
				lowCut = score;
				if (cumulative * 3 >= total) {
					break;
				}
			}

			var highCut = TriRankAspects.MAX_SCORE;
			cumulative = 0;
			for (var score = TriRankAspects.MAX_SCORE; score >= TriRankAspects.MIN_SCORE; score--) {
				cumulative += histogram[score];
				highCut = score;
				if (cumulative * 3 >= total) {
					break;
				}
			}

			if (lowCut >= highCut) {
				// Not enough spread for terciles to separate; fall back to the natural bipolar cut
				// around the midpoint, which leaves a constant attribute contributing no edges at all.
				lowCut = (int)TriRankAspects.NEUTRAL_SCORE - 1;
				highCut = (int)TriRankAspects.NEUTRAL_SCORE + 1;
			}
			cutoffByAttributeId.put(attributeId, new PoleCutoff(lowCut, highCut));
		});
		return cutoffByAttributeId;
	}

	private Map<UserCourseKey, List<UserCourseRating>> groupByUserCourse(List<UserCourseRating> userCourseRatings,
		Map<Long, String> courseIdToCourseCode) {
		return userCourseRatings.stream()
			.filter(rating -> courseIdToCourseCode.containsKey(rating.getCourseId()))
			.sorted(Comparator.comparing(UserCourseRating::getUserId)
				.thenComparing(rating -> courseIdToCourseCode.get(rating.getCourseId()))
				.thenComparing(UserCourseRating::getAttributeId))
			.collect(Collectors.groupingBy(
				rating -> new UserCourseKey(rating.getUserId(), courseIdToCourseCode.get(rating.getCourseId())),
				LinkedHashMap::new,
				Collectors.toList()));
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

	record UserCourseKey(String userId, String courseCode) {
	}

	/** Score at or below which a rating leans low, and at or above which it leans high. */
	private record PoleCutoff(int lowCut, int highCut) {
	}
}

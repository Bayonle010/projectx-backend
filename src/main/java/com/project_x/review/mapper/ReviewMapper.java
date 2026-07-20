package com.project_x.review.mapper;

import com.project_x.review.dto.request.SavePropertyReviewRequest;
import com.project_x.review.dto.response.*;
import com.project_x.review.entity.PropertyReview;
import com.project_x.review.entity.ReviewComment;
import com.project_x.review.enums.ReviewStatus;
import com.project_x.user.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

@Component
public class ReviewMapper {

    public PropertyReview toEntity(
            SavePropertyReviewRequest request
    ) {
        return PropertyReview.builder()
                .overallRating(request.overallRating())
                .cleanlinessRating(request.cleanlinessRating())
                .communicationRating(request.communicationRating())
                .accuracyRating(request.accuracyRating())
                .valueForMoneyRating(request.valueForMoneyRating())
                .reviewText(normalizeOptionalText(request.reviewText()))
                .status(ReviewStatus.ACTIVE)
                .build();
    }

    public PropertyReviewResponse toResponse(
            PropertyReview review,
            long likeCount,
            long commentCount,
            boolean likedByCurrentUser
    ) {
        return new PropertyReviewResponse(
                review.getId(),
                review.getListing().getId(),
                toReviewerResponse(review.getReviewer()),
                review.getOverallRating(),
                new RatingBreakdownResponse(
                        review.getCleanlinessRating(),
                        review.getCommunicationRating(),
                        review.getAccuracyRating(),
                        review.getValueForMoneyRating()
                ),
                review.getReviewText(),
                likeCount,
                commentCount,
                likedByCurrentUser,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    public ReviewCommentResponse toCommentResponse(
            ReviewComment comment
    ) {
        return new ReviewCommentResponse(
                comment.getId(),
                comment.getReview().getId(),
                toReviewerResponse(comment.getAuthor()),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public ReviewerResponse toReviewerResponse(User user) {
        return new ReviewerResponse(
                user.getId(),
                buildDisplayName(user)
        );
    }

    public double roundRating(Double rating) {
        if (rating == null) {
            return 0.0;
        }

        return BigDecimal.valueOf(rating)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String buildDisplayName(User user) {
      
        return Stream.of(
                        user.getFirstname(),
                        user.getLastname()
                )
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .reduce((first, second) -> first + " " + second)
                .orElse("Anonymous User");
    }
}
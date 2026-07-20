package com.project_x.review.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PropertyReviewResponse(
        UUID id,
        UUID listingId,
        ReviewerResponse reviewer,
        int overallRating,
        RatingBreakdownResponse ratingBreakdown,
        String reviewText,
        long likeCount,
        long commentCount,
        boolean likedByCurrentUser,
        Instant createdAt,
        Instant updatedAt
) {
}
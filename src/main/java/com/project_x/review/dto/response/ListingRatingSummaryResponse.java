package com.project_x.review.dto.response;

import java.util.UUID;

public record ListingRatingSummaryResponse(
        UUID listingId,
        long totalReviews,
        double averageRating,
        RatingBreakdownResponse ratingBreakdown
) {
}
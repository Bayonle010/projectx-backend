package com.project_x.review.dto.response;

public record OwnerReviewDashboardResponse(
        long totalProperties,
        long totalReviews,
        double averageRating,
        RatingBreakdownResponse ratingBreakdown
) {
}
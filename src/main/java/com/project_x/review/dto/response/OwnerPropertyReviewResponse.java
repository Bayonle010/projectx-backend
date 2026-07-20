package com.project_x.review.dto.response;

import java.util.UUID;

public record OwnerPropertyReviewResponse(
        UUID listingId,
        String address,
        long totalReviews,
        double averageRating
) {
}
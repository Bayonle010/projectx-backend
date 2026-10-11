package com.project_x.review.projection;

import java.util.UUID;

public interface OwnerPropertyReviewProjection {
    UUID getListingId();

    String getFriendlyId();

    String getCoverImageUrl();

    String getAddress();

    Long getTotalReviews();

    Double getAverageRating();
}

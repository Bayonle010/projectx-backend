package com.project_x.review.projection;

public interface ListingRatingSummaryProjection {
    Long getTotalReviews();

    Double getAverageRating();

    Double getCleanlinessRating();

    Double getCommunicationRating();

    Double getAccuracyRating();

    Double getValueForMoneyRating();
}

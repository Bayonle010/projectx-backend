package com.project_x.review.dto.response;

public record RatingBreakdownResponse(
        double cleanliness,
        double communication,
        double accuracy,
        double valueForMoney
) {}
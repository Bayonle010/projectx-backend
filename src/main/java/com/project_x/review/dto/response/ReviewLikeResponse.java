package com.project_x.review.dto.response;

import java.util.UUID;

public record ReviewLikeResponse(
        UUID reviewId,
        boolean liked,
        long totalLikes
) {
}
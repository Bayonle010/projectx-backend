package com.project_x.review.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReviewCommentResponse(
        UUID id,
        UUID reviewId,
        ReviewerResponse author,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
}
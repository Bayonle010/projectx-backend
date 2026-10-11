package com.project_x.review.dto.response;

import java.util.UUID;

public record ReviewCommentLikeResponse(
        UUID commentId,
        boolean liked,
        long totalLikes
) {
}

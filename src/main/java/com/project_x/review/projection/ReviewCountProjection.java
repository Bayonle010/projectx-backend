package com.project_x.review.projection;

import java.util.UUID;

public interface ReviewCountProjection {
    UUID getReviewId();

    Long getTotal();
}

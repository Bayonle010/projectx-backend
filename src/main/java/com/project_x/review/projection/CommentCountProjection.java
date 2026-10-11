package com.project_x.review.projection;

import java.util.UUID;

public interface CommentCountProjection {
    UUID getCommentId();

    Long getTotal();
}

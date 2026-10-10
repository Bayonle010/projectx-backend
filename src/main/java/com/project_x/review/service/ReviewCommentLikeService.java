package com.project_x.review.service;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.response.ReviewCommentLikeResponse;

import java.util.UUID;

public interface ReviewCommentLikeService {

    ReviewCommentLikeResponse like(
            UUID commentId,
            AuthenticationIdentity auth
    );

    ReviewCommentLikeResponse unlike(
            UUID commentId,
            AuthenticationIdentity auth
    );
}

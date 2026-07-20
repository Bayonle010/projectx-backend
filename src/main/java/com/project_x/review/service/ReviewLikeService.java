package com.project_x.review.service;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.response.ReviewLikeResponse;

import java.util.UUID;

public interface ReviewLikeService {

    ReviewLikeResponse like(
            UUID reviewId,
            AuthenticationIdentity auth
    );

    ReviewLikeResponse unlike(
            UUID reviewId,
            AuthenticationIdentity auth
    );
}
package com.project_x.review.service;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.request.SaveReviewCommentRequest;
import com.project_x.review.dto.response.ReviewCommentResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ReviewCommentService {

    ReviewCommentResponse create(
            UUID reviewId,
            SaveReviewCommentRequest request,
            AuthenticationIdentity auth
    );

    Page<ReviewCommentResponse> getComments(
            UUID reviewId,
            Long page,
            Long pageSize
    );
}
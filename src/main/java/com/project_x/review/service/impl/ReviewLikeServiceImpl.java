package com.project_x.review.service.impl;

import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.response.ReviewLikeResponse;
import com.project_x.review.enums.ReviewStatus;
import com.project_x.review.repository.PropertyReviewRepository;
import com.project_x.review.repository.ReviewLikeRepository;
import com.project_x.review.service.ReviewLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewLikeServiceImpl implements ReviewLikeService {

    private final PropertyReviewRepository reviewRepository;
    private final ReviewLikeRepository likeRepository;

    @Override
    @Transactional
    public ReviewLikeResponse like(
            UUID reviewId,
            AuthenticationIdentity auth
    ) {
        validateReview(reviewId);

        UUID userId = UUID.fromString(auth.getId());

        likeRepository.insertIfAbsent(
                UUID.randomUUID(),
                reviewId,
                userId
        );

        long totalLikes =
                likeRepository.countByReview_Id(reviewId);

        return new ReviewLikeResponse(
                reviewId,
                true,
                totalLikes
        );
    }

    @Override
    @Transactional
    public ReviewLikeResponse unlike(
            UUID reviewId,
            AuthenticationIdentity auth
    ) {
        validateReview(reviewId);

        likeRepository.deleteByReview_IdAndUser_Id(
                reviewId,
                UUID.fromString(auth.getId())
        );

        long totalLikes =
                likeRepository.countByReview_Id(reviewId);

        return new ReviewLikeResponse(
                reviewId,
                false,
                totalLikes
        );
    }

    private void validateReview(UUID reviewId) {
        if (!reviewRepository.existsByIdAndStatus(
                reviewId,
                ReviewStatus.ACTIVE
        )) {
            throw new ResourceNotFoundException(
                    "Property review not found"
            );
        }
    }
}

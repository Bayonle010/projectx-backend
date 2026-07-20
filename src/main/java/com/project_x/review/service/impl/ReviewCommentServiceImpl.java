package com.project_x.review.service.impl;

import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.paginationhelper.PaginationAdapters;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.request.SaveReviewCommentRequest;
import com.project_x.review.dto.response.ReviewCommentResponse;
import com.project_x.review.entity.PropertyReview;
import com.project_x.review.entity.ReviewComment;
import com.project_x.review.enums.ReviewCommentStatus;
import com.project_x.review.enums.ReviewStatus;
import com.project_x.review.mapper.ReviewMapper;
import com.project_x.review.repository.PropertyReviewRepository;
import com.project_x.review.repository.ReviewCommentRepository;
import com.project_x.review.service.ReviewCommentService;
import com.project_x.user.entity.User;
import com.project_x.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewCommentServiceImpl
        implements ReviewCommentService {

    private final PropertyReviewRepository reviewRepository;
    private final ReviewCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewCommentResponse create(
            UUID reviewId,
            SaveReviewCommentRequest request,
            AuthenticationIdentity auth
    ) {
        PropertyReview review =
                reviewRepository.findByIdAndStatus(
                                reviewId,
                                ReviewStatus.ACTIVE
                        )
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Property review not found"
                        ));

        UUID authenticatedUserId = UUID.fromString(auth.getId());
        User author =
                userRepository.getReferenceById(authenticatedUserId);

        ReviewComment comment = ReviewComment.builder()
                .review(review)
                .author(author)
                .content(request.content().trim())
                .status(ReviewCommentStatus.ACTIVE)
                .build();

        ReviewComment savedComment =
                commentRepository.save(comment);

        return reviewMapper.toCommentResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewCommentResponse> getComments(
            UUID reviewId,
            Long page,
            Long pageSize
    ) {
        Pageable pageable =
                PaginationAdapters.createPageRequestWithRecentFirstsSortOrder(
                        page,
                        pageSize
                );

        return commentRepository
                .findByReview_IdAndStatusOrderByCreatedAtAsc(
                        reviewId,
                        ReviewCommentStatus.ACTIVE,
                        pageable
                )
                .map(reviewMapper::toCommentResponse);
    }
}
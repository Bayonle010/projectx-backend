package com.project_x.review.service.impl;

import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.response.ReviewCommentLikeResponse;
import com.project_x.review.enums.ReviewCommentStatus;
import com.project_x.review.enums.ReviewStatus;
import com.project_x.review.repository.ReviewCommentLikeRepository;
import com.project_x.review.repository.ReviewCommentRepository;
import com.project_x.review.service.ReviewCommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewCommentLikeServiceImpl
        implements ReviewCommentLikeService {

    private final ReviewCommentRepository commentRepository;
    private final ReviewCommentLikeRepository commentLikeRepository;

    @Override
    @Transactional
    public ReviewCommentLikeResponse like(
            UUID commentId,
            AuthenticationIdentity auth
    ) {
        validateComment(commentId);
        UUID userId = UUID.fromString(auth.getId());

        commentLikeRepository.insertIfAbsent(
                UUID.randomUUID(),
                commentId,
                userId
        );

        return response(commentId, true);
    }

    @Override
    @Transactional
    public ReviewCommentLikeResponse unlike(
            UUID commentId,
            AuthenticationIdentity auth
    ) {
        validateComment(commentId);

        commentLikeRepository.deleteByComment_IdAndUser_Id(
                commentId,
                UUID.fromString(auth.getId())
        );

        return response(commentId, false);
    }

    private void validateComment(UUID commentId) {
        if (!commentRepository.existsByIdAndStatusAndReview_Status(
                commentId,
                ReviewCommentStatus.ACTIVE,
                ReviewStatus.ACTIVE
        )) {
            throw new ResourceNotFoundException(
                    "Review comment not found"
            );
        }
    }

    private ReviewCommentLikeResponse response(
            UUID commentId,
            boolean liked
    ) {
        return new ReviewCommentLikeResponse(
                commentId,
                liked,
                commentLikeRepository.countByComment_Id(commentId)
        );
    }
}
